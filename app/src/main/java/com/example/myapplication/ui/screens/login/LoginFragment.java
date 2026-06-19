package com.example.myapplication.ui.screens.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentLoginBinding;
import com.example.myapplication.util.SessionManager;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private NavController navController;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        sessionManager = SessionManager.getInstance(requireContext());
        setupListeners();
    }

    private void setupListeners() {
        binding.loginBtn.setOnClickListener(v -> handleLogin());
        binding.registerLink.setOnClickListener(v ->
                navController.navigate(R.id.action_login_to_register));
        binding.forgotPasswordLink.setOnClickListener(v ->
                Toast.makeText(requireContext(), "请联系客服重置密码", Toast.LENGTH_SHORT).show());

        binding.phoneInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { validateInputs(); }
        });

        binding.passwordInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { validateInputs(); }
        });
    }

    private void handleLogin() {
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

        binding.loginBtn.setEnabled(false);
        binding.loginBtn.setText(R.string.loading);

        sessionManager.login(phone, password, new SessionManager.AuthCallback() {
            @Override
            public void onSuccess() {
                binding.loginBtn.post(() -> {
                    binding.loginBtn.setEnabled(true);
                    binding.loginBtn.setText(R.string.login);
                    Toast.makeText(requireContext(), R.string.success_login, Toast.LENGTH_SHORT).show();
                    navController.navigate(R.id.action_login_to_home);
                });
            }
            @Override
            public void onError(String error) {
                binding.loginBtn.post(() -> {
                    binding.loginBtn.setEnabled(true);
                    binding.loginBtn.setText(R.string.login);
                    Toast.makeText(requireContext(), "登录失败: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void validateInputs() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
        binding.loginBtn.setEnabled(phone.length() == 11 && password.length() >= 6);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
