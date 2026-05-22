package com.example.myapplication.ui.screens.register;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentRegisterBinding;
import com.google.android.material.button.MaterialButton;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private NavController navController;
    private int passwordStrength = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        setupListeners();
    }

    private void setupListeners() {
        // Back button
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });

        // Password strength indicator
        binding.passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordStrength(s.toString());
                validateInputs();
            }
        });

        // Phone input
        binding.phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });

        // Agreement checkbox
        binding.agreeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            validateInputs();
        });

        // Register button
        binding.registerBtn.setOnClickListener(v -> handleRegister());
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            binding.strengthIndicator.setVisibility(View.GONE);
            passwordStrength = 0;
            return;
        }

        binding.strengthIndicator.setVisibility(View.VISIBLE);

        int strength = 0;
        if (password.length() >= 6) strength++;
        if (password.length() >= 10) strength++;
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[^a-zA-Z0-9].*")) strength++;

        passwordStrength = Math.min(strength, 3);

        // Update bar colors
        int activeColor = R.color.error;
        String strengthTextStr = getString(R.string.password_strength_weak);

        if (passwordStrength >= 3) {
            activeColor = R.color.success;
            strengthTextStr = getString(R.string.password_strength_strong);
        } else if (passwordStrength >= 2) {
            activeColor = R.color.warning;
            strengthTextStr = getString(R.string.password_strength_medium);
        }

        int color = ContextCompat.getColor(requireContext(), activeColor);

        binding.strengthBar1.setBackgroundColor(passwordStrength >= 1 ? color :
                ContextCompat.getColor(requireContext(), R.color.border_color));
        binding.strengthBar2.setBackgroundColor(passwordStrength >= 2 ? color :
                ContextCompat.getColor(requireContext(), R.color.border_color));
        binding.strengthBar3.setBackgroundColor(passwordStrength >= 3 ? color :
                ContextCompat.getColor(requireContext(), R.color.border_color));

        binding.strengthText.setText(strengthTextStr);
        binding.strengthText.setTextColor(color);
    }

    private void validateInputs() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
        boolean agreed = binding.agreeCheckbox.isChecked();

        boolean phoneValid = phone.length() == 11;
        boolean passwordValid = password.length() >= 6;

        binding.registerBtn.setEnabled(phoneValid && passwordValid && agreed);
    }

    private void handleRegister() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";

        if (phone.length() != 11) {
            Toast.makeText(requireContext(), R.string.error_invalid_phone, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(requireContext(), R.string.error_short_password, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!binding.agreeCheckbox.isChecked()) {
            Toast.makeText(requireContext(), R.string.error_agree_terms, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.registerBtn.setEnabled(false);
        binding.registerBtn.setText(R.string.loading);

        // TODO: Call API to register
        // For now, simulate successful registration
        binding.registerBtn.postDelayed(() -> {
            Toast.makeText(requireContext(), R.string.success_register, Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.action_register_to_assessment);
        }, 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
