package com.bodakesatish.sandhyasbeautyservices.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.launch
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentLoginBinding
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            // Basic client-side validation (optional, but good UX)
            if (email.isEmpty() || password.isEmpty()) {

                Snackbar.make(binding.root, "Email and password cannot be empty.",
                    Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }

        binding.textViewSignUp.setOnClickListener {
            findNavController().navigate(R.id.login_to_register) // Example navigation
        }

        observeLoginState()
        observeCurrentAuthState() // To auto-navigate if already logged in

    }

    private fun observeCurrentAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentAuthState.collect { firebaseUser ->
                    if (firebaseUser != null) {
                        // User is already logged in, navigate to home/dashboard
                        Log.d(TAG, "User already logged in: ${firebaseUser.uid}, navigating to home.")
                        // findNavController().navigate(R.id.action_loginFragment_to_homeFragment) // Replace with your home destination
                        // To prevent navigating back to login, you might want to popUpTo
                        findNavController().navigate(
                            LoginFragmentDirections.loginToHome() // Example using Safe Args
                        )
                        viewModel.consumeLoginOperationState() // Reset if any pending login op
                    }
                }
            }
        }
    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginOperationState.collect { result ->
                    // Hide loading indicator by default, show it only for NetworkResult.Loading
                    binding.progressBar.isVisible = false // Assuming you have a ProgressBar with id "progressBar"
                    binding.buttonLogin.isEnabled = true // Enable button by default

                    when (result) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "Login successful for user: ${result.data.email}")
                            Snackbar.make(binding.root, "Login Successful!", Snackbar.LENGTH_SHORT).show()
                            // Navigation should ideally happen by observing currentAuthState,
                            // or you can navigate directly here if currentAuthState doesn't immediately reflect the new user.
                            // For immediate navigation after login success:
                            findNavController().navigate(
                                LoginFragmentDirections.loginToHome() // Example using Safe Args
                            )
                            viewModel.consumeLoginOperationState() // Reset the state after handling
                        }
                        is NetworkResult.Error -> {
                            Log.e(TAG, "Login failed: ${result.message} (Code: ${result.code})", result.exception)
                            val errorMessage = result.message + if (result.code != null) " (Code: ${result.code})" else ""
                            Snackbar.make(binding.root, "Login Failed: $errorMessage", Snackbar.LENGTH_LONG).show()
                        }
                        is NetworkResult.Loading -> {
                            Log.d(TAG, "Login in progress...")
                            binding.progressBar.isVisible = true
                            binding.buttonLogin.isEnabled = false
                            // Optionally show a loading message or disable input fields
                        }
                        is NetworkResult.NoInternet -> {
                            Log.w(TAG, "Login attempt failed: No Internet Connection")
                            Snackbar.make(binding.root, "No Internet Connection. Please check your network.", Snackbar.LENGTH_LONG).show()
                        }
                        null -> {
                            // Initial state or consumed state, do nothing
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}