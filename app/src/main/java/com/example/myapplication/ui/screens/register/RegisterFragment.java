package com.example.myapplication.ui.screens.register;

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
import com.example.myapplication.databinding.FragmentRegisterBinding;
import com.example.myapplication.util.SessionManager;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
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
        sessionManager = SessionManager.getInstance(requireContext());
        setupListeners();
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> navController.popBackStack());

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
                updatePasswordStrength(s.toString());
                validatePassword(s.toString());
                validateConfirmPassword(binding.confirmPasswordInput.getText().toString());
                validateInputs();
            }
        });

        binding.confirmPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validateConfirmPassword(s.toString());
                validateInputs();
            }
        });

        binding.agreeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> validateInputs());
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
        binding.strengthBar1.setBackgroundColor(passwordStrength >= 1 ? color : ContextCompat.getColor(requireContext(), R.color.border_color));
        binding.strengthBar2.setBackgroundColor(passwordStrength >= 2 ? color : ContextCompat.getColor(requireContext(), R.color.border_color));
        binding.strengthBar3.setBackgroundColor(passwordStrength >= 3 ? color : ContextCompat.getColor(requireContext(), R.color.border_color));
        binding.strengthText.setText(strengthTextStr);
        binding.strengthText.setTextColor(color);
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
            binding.phoneInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.success));
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
            binding.passwordInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.success));
        }
    }

    private void validateConfirmPassword(String confirmPassword) {
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordError.setVisibility(View.GONE);
            binding.confirmPasswordInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.border_color));
        } else if (!confirmPassword.equals(password)) {
            binding.confirmPasswordError.setText("两次输入的密码不一致");
            binding.confirmPasswordError.setVisibility(View.VISIBLE);
            binding.confirmPasswordInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.error));
        } else {
            binding.confirmPasswordError.setVisibility(View.GONE);
            binding.confirmPasswordInputLayout.setBoxStrokeColor(ContextCompat.getColor(requireContext(), R.color.success));
        }
    }

    private void validateInputs() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
        String confirmPassword = binding.confirmPasswordInput.getText() != null ? binding.confirmPasswordInput.getText().toString() : "";
        boolean agreed = binding.agreeCheckbox.isChecked();
        binding.registerBtn.setEnabled(phone.length() == 11 && password.length() >= 6 && password.equals(confirmPassword) && agreed);
    }

    private void handleRegister() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";
        String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
        String confirmPassword = binding.confirmPasswordInput.getText() != null ? binding.confirmPasswordInput.getText().toString() : "";

        // 实时校验已经在输入时显示，这里做最终检查
        validatePhone(phone);
        validatePassword(password);
        validateConfirmPassword(confirmPassword);

        if (phone.length() != 11 || !phone.startsWith("1")) {
            return;
        }
        if (password.length() < 6) {
            return;
        }
        if (!password.equals(confirmPassword)) {
            return;
        }
        if (!binding.agreeCheckbox.isChecked()) {
            Toast.makeText(requireContext(), R.string.error_agree_terms, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.registerBtn.setEnabled(false);
        binding.registerBtn.setText(R.string.loading);

        // 调用后端 API 注册
        sessionManager.register(phone, password, "健身爱好者", new SessionManager.AuthCallback() {
            @Override
            public void onSuccess() {
                binding.registerBtn.post(() -> {
                    Toast.makeText(requireContext(), R.string.success_register, Toast.LENGTH_SHORT).show();
                    navController.navigate(R.id.action_register_to_assessment);
                });
            }
            @Override
            public void onError(String error) {
                binding.registerBtn.post(() -> {
                    binding.registerBtn.setEnabled(true);
                    binding.registerBtn.setText(R.string.register_btn);
                    String errorMsg;
                    if (error.contains("already exists") || error.contains("already registered") || error.contains("400")) {
                        errorMsg = "手机号已被注册";
                    } else if (error.contains("invalid phone") || error.contains("phone")) {
                        errorMsg = "手机号格式不正确";
                    } else if (error.contains("timeout") || error.contains("connect")) {
                        errorMsg = "网络连接失败，请检查网络";
                    } else {
                        errorMsg = "注册失败，请稍后重试";
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
