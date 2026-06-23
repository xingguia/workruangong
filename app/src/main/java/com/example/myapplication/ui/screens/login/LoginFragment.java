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
import androidx.core.content.ContextCompat;
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
            @Override public void afterTextChanged(Editable s) {
                validatePhone(s.toString());
                validateInputs();
            }
        });

        binding.passwordInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
                validateInputs();
            }
        });
    }

    private void handleLogin() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";

        // 实时校验已经在输入时显示，这里做最终检查
        validatePhone(phone);
        validatePassword(password);

        if (phone.length() != 11 || !phone.startsWith("1")) {
            return;
        }
        if (password.length() < 6) {
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
                    String errorMsg;
                    if (error.contains("Invalid phone or password") || error.contains("401")) {
                        errorMsg = "手机号或密码错误";
                    } else if (error.contains("not found") || error.contains("404")) {
                        errorMsg = "用户不存在";
                    } else if (error.contains("该账号已被封禁") || error.contains("403")) {
                        errorMsg = "该账号已被封禁，请联系管理员";
                    } else if (error.contains("timeout") || error.contains("connect")) {
                        errorMsg = "网络连接失败，请检查网络";
                    } else {
                        errorMsg = "登录失败，请稍后重试";
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void validateInputs() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
        binding.loginBtn.setEnabled(phone.length() == 11 && password.length() >= 6);
    }

    private void validatePhone(String phone) {
        if (phone.isEmpty()) {
            binding.phoneError.setVisibility(View.GONE);
            binding.phoneInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.border_color));
        } else if (phone.length() < 11) {
            binding.phoneError.setText("手机号需要11位，当前" + phone.length() + "位");
            binding.phoneError.setVisibility(View.VISIBLE);
            binding.phoneInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.error));
        } else if (!phone.startsWith("1")) {
            binding.phoneError.setText("手机号必须以1开头");
            binding.phoneError.setVisibility(View.VISIBLE);
            binding.phoneInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.error));
        } else {
            binding.phoneError.setVisibility(View.GONE);
            binding.phoneInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.border_color));
        }
    }

    private void validatePassword(String password) {
        if (password.isEmpty()) {
            binding.passwordError.setVisibility(View.GONE);
            binding.passwordInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.border_color));
        } else if (password.length() < 6) {
            binding.passwordError.setText("密码至少需要6位，当前" + password.length() + "位");
            binding.passwordError.setVisibility(View.VISIBLE);
            binding.passwordInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.error));
        } else {
            binding.passwordError.setVisibility(View.GONE);
            binding.passwordInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.border_color));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
