package com.bodakesatish.sandhyasbeautyservices.signup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bodakesatish.sandhyasbeautyservices.R
import com.bodakesatish.sandhyasbeautyservices.databinding.FragmentSignupBinding
import com.bodakesatish.sandhyasbeautyservices.domain.utils.NetworkResult
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()
    private val TAG = "RegisterFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            //val confirmPassword = binding.editTextConfirmPassword.text.toString().trim() // Assuming you have confirm password
            val confirmPassword = password
            val fullName = binding.editTextName.text.toString().trim()

            // Basic client-side validation
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Snackbar.make(
                    binding.root, "All fields are required.",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Snackbar.make(
                    binding.root, "Passwords do not match.",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // Add more validation if needed (e.g., password strength, email format)

            viewModel.register(
                email = email,
                pass = password,
                fullName = fullName
            )
        }

        binding.textViewSignIn.setOnClickListener { // Assuming you have a login link
            findNavController().navigate(R.id.register_to_login) // Adjust navigation action
        }

        observeRegistrationState()
        observeCurrentAuthState() // To auto-navigate if registration leads to immediate login

    }

    private fun observeCurrentAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentAuthState.collect { firebaseUser ->
                    if (firebaseUser != null && (viewModel.registrationOperationState.value is NetworkResult.Success<*> || viewModel.registrationOperationState.value == null)) {
                        // User is logged in (potentially after successful registration)
                        // and we are not in a failed registration state.
                        Log.d(
                            TAG,
                            "User is now logged in: ${firebaseUser.uid}, navigating to home."
                        )
                        // Navigate to home/dashboard
                        // Replace with your actual navigation action (Safe Args recommended)
                        // Example: findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToHomeFragment())
                        findNavController().navigate(R.id.register_to_home) // Replace with your home destination

                        // Consume state to prevent re-navigation if currentAuthState emits again before fragment is destroyed
                        viewModel.consumeRegistrationOperationState()
                    }
                }
            }
        }
    }

    private fun observeRegistrationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationOperationState.collect { result ->
                    // Hide loading indicator by default, show it only for NetworkResult.Loading
                    binding.progressBar.isVisible =  false // Assuming ProgressBar id="progressBarSignup"
                    binding.buttonRegister.isEnabled = true // Enable button by default

                    when (result) {
                        is NetworkResult.Success -> {
                            Log.d(TAG, "Registration successful for user: ${result.data.email}")
                            Snackbar.make(
                                binding.root, "Registration Successful!",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            // Navigation to home is typically handled by `observeCurrentAuthState`
                            // as registration often implies immediate login.
                            // If not, navigate here:
                            // findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                            // viewModel.consumeRegistrationOperationState() // Consume here if navigating directly
                        }

                        is NetworkResult.Error -> {
                            Log.e(
                                TAG,
                                "Registration failed: ${result.message} (Code: ${result.code})",
                                result.exception
                            )
                            val errorMessage =
                                result.message + if (result.code != null) " (Code: ${result.code})" else ""
                            Snackbar.make(
                                binding.root, "Registration Failed: $errorMessage",
                                Snackbar.LENGTH_LONG
                            ).show()
                            // viewModel.consumeRegistrationOperationState() // Allow user to retry
                        }

                        is NetworkResult.Loading -> {
                            Log.d(TAG, "Registration in progress...")
                            binding.progressBar.isVisible = true
                            binding.buttonRegister.isEnabled = false
                        }

                        is NetworkResult.NoInternet -> {
                            Log.w(TAG, "Registration attempt failed: No Internet Connection")
                            Snackbar.make(
                                binding.root, "No Internet Connection. Please check your network.",
                                Snackbar.LENGTH_LONG
                            ).show()
                            // viewModel.consumeRegistrationOperationState() // Allow user to retry
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