package com.example.myapplication.ui.screens.login;

import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.example.myapplication.api.ApiClient;
import com.example.myapplication.databinding.FragmentLoginBinding;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private boolean isSmsMode = true;
    private CountDownTimer countDownTimer;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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
        // Tab switch
        binding.tabSms.setOnClickListener(v -> switchToSmsMode());
        binding.tabPassword.setOnClickListener(v -> switchToPasswordMode());

        // Get verification code
        binding.getCodeBtn.setOnClickListener(v -> sendVerificationCode());

        // Login
        binding.loginBtn.setOnClickListener(v -> handleLogin());

        // Register link
        binding.registerLink.setOnClickListener(v -> {
            navController.navigate(R.id.action_login_to_register);
        });

        // WeChat login (placeholder)
        binding.wechatLoginBtn.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "微信登录功能开发中", Toast.LENGTH_SHORT).show();
        });

        // Input validation
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

        binding.codeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });

        binding.passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });
    }

    private void switchToSmsMode() {
        isSmsMode = true;
        binding.tabSms.setBackgroundTintList(requireContext().getColorStateList(R.color.primary));
        binding.tabSms.setTextColor(requireContext().getColor(R.color.white));
        binding.tabPassword.setBackgroundTintList(null);
        binding.tabPassword.setTextColor(requireContext().getColor(R.color.text_secondary));

        binding.codeInputLayout.setVisibility(View.VISIBLE);
        binding.getCodeBtn.setVisibility(View.VISIBLE);
        binding.passwordInputLayout.setVisibility(View.GONE);
        binding.forgotPasswordLink.setVisibility(View.GONE);

        validateInputs();
    }

    private void switchToPasswordMode() {
        isSmsMode = false;
        binding.tabPassword.setBackgroundTintList(requireContext().getColorStateList(R.color.primary));
        binding.tabPassword.setTextColor(requireContext().getColor(R.color.white));
        binding.tabSms.setBackgroundTintList(null);
        binding.tabSms.setTextColor(requireContext().getColor(R.color.text_secondary));

        binding.codeInputLayout.setVisibility(View.GONE);
        binding.getCodeBtn.setVisibility(View.GONE);
        binding.passwordInputLayout.setVisibility(View.VISIBLE);
        binding.forgotPasswordLink.setVisibility(View.VISIBLE);

        validateInputs();
    }

    private void sendVerificationCode() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";

        if (phone.length() != 11) {
            Toast.makeText(requireContext(), R.string.error_invalid_phone, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.getCodeBtn.setEnabled(false);
        binding.getCodeBtn.setText("发送中...");

        // TODO: Call API to send verification code
        // Simulating API call
        startCountdown();

        Toast.makeText(requireContext(), R.string.success_send_code, Toast.LENGTH_SHORT).show();
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.getCodeBtn.setText((millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                binding.getCodeBtn.setEnabled(true);
                binding.getCodeBtn.setText(R.string.get_code);
            }
        }.start();
    }

    private void handleLogin() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";

        if (phone.length() != 11) {
            Toast.makeText(requireContext(), R.string.error_invalid_phone, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSmsMode) {
            String code = binding.codeInput.getText() != null ? binding.codeInput.getText().toString() : "";
            if (code.length() != 6) {
                Toast.makeText(requireContext(), R.string.error_invalid_code, Toast.LENGTH_SHORT).show();
                return;
            }
            loginWithSms(phone, code);
        } else {
            String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
            if (password.length() < 6) {
                Toast.makeText(requireContext(), R.string.error_short_password, Toast.LENGTH_SHORT).show();
                return;
            }
            loginWithPassword(phone, password);
        }
    }

    private void loginWithSms(String phone, String code) {
        binding.loginBtn.setEnabled(false);
        binding.loginBtn.setText(R.string.loading);

        // TODO: Call API to login with SMS
        // For now, simulate successful login
        simulateLoginSuccess();
    }

    private void loginWithPassword(String phone, String password) {
        binding.loginBtn.setEnabled(false);
        binding.loginBtn.setText(R.string.loading);

        // TODO: Call API to login with password
        // For now, simulate successful login
        simulateLoginSuccess();
    }

    private void simulateLoginSuccess() {
        // Generate a mock user ID and token
        String userId = "user_" + System.currentTimeMillis();
        String token = "token_" + System.currentTimeMillis();
        String phone = binding.phoneInput.getText().toString();

        // 保留已有的昵称，仅首次登录时使用默认值
        String existingNickname = sessionManager.getNickname();
        String nickname = (existingNickname != null && !existingNickname.isEmpty())
                ? existingNickname : "健身爱好者";

        // Save user session
        sessionManager.saveUserSession(userId, nickname, phone, token);

        binding.loginBtn.postDelayed(() -> {
            binding.loginBtn.setEnabled(true);
            binding.loginBtn.setText(R.string.login);
            Toast.makeText(requireContext(), R.string.success_login, Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.action_login_to_home);
        }, 1000);
    }

    private void validateInputs() {
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString() : "";
        boolean phoneValid = phone.length() == 11;

        if (isSmsMode) {
            String code = binding.codeInput.getText() != null ? binding.codeInput.getText().toString() : "";
            binding.loginBtn.setEnabled(phoneValid && code.length() == 6);
        } else {
            String password = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString() : "";
            binding.loginBtn.setEnabled(phoneValid && password.length() >= 6);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding = null;
    }
}
