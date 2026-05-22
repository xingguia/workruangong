package com.example.myapplication.ui.screens.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.util.SessionManager;
import com.example.myapplication.util.UsernameValidator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class UsernameSetDialog {

    public interface OnUsernameSetListener {
        void onUsernameSet(String username);
        void onDismiss();
    }

    private final Context context;
    private final SessionManager sessionManager;
    private AlertDialog dialog;
    private OnUsernameSetListener listener;

    public UsernameSetDialog(Context context) {
        this.context = context;
        this.sessionManager = SessionManager.getInstance(context);
    }

    public void setOnUsernameSetListener(OnUsernameSetListener listener) {
        this.listener = listener;
    }

    public void show() {
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_set_username, null);

        TextInputLayout usernameInputLayout = dialogView.findViewById(R.id.usernameInputLayout);
        TextInputEditText usernameInput = dialogView.findViewById(R.id.usernameInput);

        dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("确认", null)
                .setNegativeButton("跳过", (d, which) -> {
                    if (listener != null) {
                        listener.onDismiss();
                    }
                })
                .setCancelable(false)
                .create();

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();
                if (!username.isEmpty()) {
                    UsernameValidator.ValidationResult result = UsernameValidator.validate(username);
                    if (!result.valid) {
                        usernameInputLayout.setError(result.errorMessage);
                    } else if (sessionManager.isNicknameAvailable(username)) {
                        usernameInputLayout.setError(null);
                    } else {
                        usernameInputLayout.setError("该用户名已被使用");
                    }
                } else {
                    usernameInputLayout.setError(null);
                }
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String username = usernameInput.getText() != null ?
                        usernameInput.getText().toString().trim() : "";

                if (username.isEmpty()) {
                    usernameInputLayout.setError("请输入用户名");
                    return;
                }

                UsernameValidator.ValidationResult result = UsernameValidator.validateWithAvailability(context, username);
                if (!result.valid) {
                    usernameInputLayout.setError(result.errorMessage);
                    return;
                }

                // Save the username
                sessionManager.saveNickname(username);
                sessionManager.addUsernameToSet(username);
                sessionManager.markUsernameSet();

                dialog.dismiss();

                if (listener != null) {
                    listener.onUsernameSet(username);
                }
            });
        });

        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
