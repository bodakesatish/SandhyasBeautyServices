package com.bodakesatish.sandhyasbeautyservices.data.repository

// com/example/yourapp/data/repository/AuthRepository.kt

import android.util.Log
import com.bodakesatish.sandhyasbeautyservices.data.utils.NetworkConnectivityService
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.java
import kotlin.let

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult() // For UI state
}

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val networkConnectivityService: NetworkConnectivityService // Inject this
) {

    val tag = "AuthRepository: "

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // Flow to observe Firebase Auth state
    val authState: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }.flowOn(Dispatchers.IO)


    suspend fun register(email: String, pass: String, fullName: String): Flow<NetworkResult<FirebaseUser>> =
        flow {
            emit(NetworkResult.Loading)
            if (!networkConnectivityService.isNetworkAvailable()) {
                emit(NetworkResult.NoInternet)
                return@flow
            }
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userFirestore = UserFirestore(
                        uid = firebaseUser.uid,
                        fullName = fullName,
                        email = email,
                        isAdmin = false, // Default new users are not admins
                        registrationTimestamp = System.currentTimeMillis()
                    )
                    // Store user profile in Firestore
                    firestore.collection("admins").document(firebaseUser.uid)
                        .set(userFirestore.toHashMap()).await()

                    // Store user profile in Room
                    val userEntity = UserEntity(
                        uid = firebaseUser.uid,
                        fullName = fullName,
                        email = email,
                        registrationTimestamp = userFirestore.registrationTimestamp,
                        lastLoginTimestamp = System.currentTimeMillis() // Set initial login time
                    )
                    userDao.insertUser(userEntity)
                    emit(NetworkResult.Success(firebaseUser))
                } else {
                    emit(NetworkResult.Error("Registration failed: User is null."))
                }
            } catch (e: Exception) {
                emit(NetworkResult.Error(e.message ?: "Registration failed.", code = 0, exception = e))
            }
        }.flowOn(Dispatchers.IO)

    suspend fun login(email: String, pass: String): Flow<NetworkResult<FirebaseUser>> = flow {
        emit(NetworkResult.Loading)
        if (!networkConnectivityService.isNetworkAvailable()) {
            emit(NetworkResult.NoInternet)
            return@flow
        }
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Fetch user profile from Firestore and update/insert into Room
                // Fetch user profile from Firestore "users" collection (or "admins" if that's where profiles are)
                // Assuming general user profiles are in "users"
                val userDocRef = firestore.collection("admins").document(firebaseUser.uid)
                val firestoreDoc = userDocRef.get().await()
                // Also check if the user is an admin
                // val adminDocRef = firestore.collection("admins").document(firebaseUser.uid)
                // val adminDoc = adminDocRef.get().await()
                // val isAdmin = adminDoc.exists() && adminDoc.getBoolean("isAdmin") == true
                // You can include this isAdmin status in your UserEntity or a combined UserProfile object

                if (firestoreDoc.exists()) {
                    val userFirestoreData = firestoreDoc.toObject(UserFirestore::class.java)
                    if (userFirestoreData != null) {
                        val userEntity = UserEntity(
                            uid = firebaseUser.uid,
                            fullName = userFirestoreData.fullName,
                            email = userFirestoreData.email,
                            registrationTimestamp = userFirestoreData.registrationTimestamp,
                            lastLoginTimestamp = System.currentTimeMillis() // Update last login time
                        )
                        userDao.insertUser(userEntity) // Using insert with OnConflictStrategy.REPLACE
                    } else {
                        // Handle case where user exists in Auth but not Firestore (should ideally not happen if register is correct)
                        // Potentially create a basic profile in Firestore/Room here or log an error
                    }
            } else {
                // User exists in Auth but not in Firestore "users" collection.
                // This could happen if registration didn't complete Firestore write, or data was deleted.
                // You might want to:
                // 1. Log this as an anomaly.
                // 2. Potentially create a basic profile in Firestore "users" now.
                // 3. Or, treat this as a partial success/error depending on your app's logic.
                Log.w(tag, "User authenticated but no profile found in Firestore 'users' for UID: ${firebaseUser.uid}. Consider creating one.")
                // For now, let's proceed with login success as auth was successful.
                // The app might need to handle a missing profile downstream.
            }
            emit(NetworkResult.Success(firebaseUser)) // Login is successful from Auth point of view
        } else {
        emit(NetworkResult.Error("Login failed: User is null from Firebase."))
    }
        }  catch (e: FirebaseNetworkException) {
            Log.e(tag, "Login Network Error: ${e.message}", e)
            emit(NetworkResult.NoInternet) // Or a more specific network error
        } catch (e: FirebaseFirestoreException) {
            Log.e(tag, "Login Firestore Error: ${e.code} - ${e.message}", e)
            // Handle specific Firestore errors like PERMISSION_DENIED
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                emit(NetworkResult.Error("Permission Denied: ${e.message}", e.code.value(), e))
            } else {
                emit(NetworkResult.Error("Firestore error: ${e.message}", e.code.value(), e))
            }
        } catch (e: Exception) { // Catch more general Firebase Auth exceptions too
            Log.e(tag, "Login Generic Error: ${e.message}", e)
            emit(NetworkResult.Error(e.message ?: "Login failed due to an unexpected error.", exception = e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            firebaseAuth.signOut()
            // Clear local user data (optional, depends on your app's needs for offline access)
            currentUser?.uid?.let { userDao.deleteUser(it) } // Or userDao.clearAllUsers()
        }
    }

    // Get current logged-in user's profile from Room
    fun getLocalUserProfile(uid: String): Flow<UserEntity?> {
        return userDao.getUserById(uid).flowOn(Dispatchers.IO)
    }

    // Example of simple session time: update last login timestamp in Room
    // This is more of an "last active" than a strict session timeout.
    // For strict session timeout (e.g., auto-logout after X minutes of inactivity),
    // you'd need more complex client-side logic or server-side validation.
    suspend fun updateUserLastLoginTime(uid: String) {
        withContext(Dispatchers.IO) {
            val user = userDao.getUserByIdOnce(uid)
            user?.let {
                userDao.updateUser(it.copy(lastLoginTimestamp = System.currentTimeMillis()))
            }
        }
    }
}
