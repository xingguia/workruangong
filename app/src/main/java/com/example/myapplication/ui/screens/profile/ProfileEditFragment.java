package com.example.myapplication.ui.screens.profile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentProfileEditBinding;
import com.example.myapplication.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileEditFragment extends Fragment {

    private FragmentProfileEditBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private AlertDialog currentDialog;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private static final String[] AVATAR_COLORS = {
            "#FF6B35", "#2ED573", "#3498DB", "#9B59B6",
            "#E74C3C", "#F39C12", "#1ABC9C", "#E91E63",
            "#00BCD4", "#673AB7", "#FF5722", "#795548"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register image picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onImagePicked
        );

        // Register permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        pickImageFromGallery();
                    } else {
                        Toast.makeText(requireContext(), "需要相册权限才能选择图片", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void onImagePicked(Uri uri) {
        if (uri != null) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) {
                    inputStream.close();
                }

                if (originalBitmap != null) {
                    // Crop to circle and resize
                    Bitmap circularBitmap = createCircularBitmap(originalBitmap, 160);
                    String avatarString = bitmapToBase64(circularBitmap);

                    // Save to session
                    sessionManager.setAvatar(avatarString);

                    // Update UI
                    binding.avatarImage.setImageBitmap(circularBitmap);
                    binding.avatarImage.setVisibility(View.VISIBLE);
                    binding.avatarPlaceholder.setVisibility(View.GONE);

                    Toast.makeText(requireContext(), "头像已更换", Toast.LENGTH_SHORT).show();

                    if (currentDialog != null) {
                        currentDialog.dismiss();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "图片处理失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap createCircularBitmap(Bitmap source, int size) {
        int sourceSize = Math.min(source.getWidth(), source.getHeight());
        int xOffset = (source.getWidth() - sourceSize) / 2;
        int yOffset = (source.getHeight() - sourceSize) / 2;

        Bitmap squareBitmap = Bitmap.createBitmap(source, xOffset, yOffset, sourceSize, sourceSize);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(squareBitmap, size, size, true);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        // Create circular clip
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, 0, 0, paint);

        return output;
    }

    private void pickImageFromGallery() {
        pickImageLauncher.launch("image/*");
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses granular media permissions
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        sessionManager = SessionManager.getInstance(requireContext());

        setupHeader();
        loadUserData();
        setupListeners();
    }

    private void setupHeader() {
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });

        binding.saveBtn.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        });
    }

    private void loadUserData() {
        // Load avatar
        String avatar = sessionManager.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(avatar, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                binding.avatarImage.setImageBitmap(bitmap);
                binding.avatarImage.setVisibility(View.VISIBLE);
                binding.avatarPlaceholder.setVisibility(View.GONE);
            } catch (Exception e) {
                loadDefaultAvatar();
            }
        } else {
            loadDefaultAvatar();
        }

        // Load nickname
        String nickname = sessionManager.getNickname();
        if (nickname != null && !nickname.isEmpty()) {
            binding.nicknameValue.setText(nickname);
        }

        // Load height
        int height = sessionManager.getHeight();
        if (height > 0) {
            binding.heightValue.setText(height + " cm");
        }

        // Load weight
        float weight = sessionManager.getWeight();
        if (weight > 0) {
            binding.weightValue.setText(String.format("%.1f kg", weight));
        }

        // Load gender
        String gender = sessionManager.getGender();
        if (gender != null && !gender.isEmpty()) {
            binding.genderValue.setText(gender);
        }

        // Load goal
        String goal = sessionManager.getFitnessGoal();
        if (goal != null && !goal.isEmpty()) {
            binding.goalValue.setText(goal);
        }
    }

    private void loadDefaultAvatar() {
        String nickname = sessionManager.getNickname();
        if (nickname != null && !nickname.isEmpty()) {
            binding.avatarPlaceholder.setText(String.valueOf(nickname.charAt(0)).toUpperCase());
        } else {
            binding.avatarPlaceholder.setText("U");
        }
    }

    private void setupListeners() {
        // Avatar click - show avatar picker
        View avatarContainer = binding.getRoot().findViewById(R.id.avatarContainer);
        avatarContainer.setOnClickListener(v -> showAvatarPickerDialog());

        // Nickname
        binding.nicknameItem.setOnClickListener(v -> showEditNicknameDialog());

        // Gender
        binding.genderItem.setOnClickListener(v -> showGenderDialog());

        // Height
        binding.heightItem.setOnClickListener(v -> showEditHeightDialog());

        // Weight
        binding.weightItem.setOnClickListener(v -> showEditWeightDialog());

        // Goal
        binding.goalItem.setOnClickListener(v -> showGoalDialog());
    }

    private void showAvatarPickerDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_avatar_picker, null);

        currentDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Add gallery picker button
        View btnPickGallery = dialogView.findViewById(R.id.btnPickFromGallery);
        if (btnPickGallery != null) {
            btnPickGallery.setOnClickListener(v -> {
                checkPermissionAndPickImage();
            });
        }

        // Avatar color options
        View[] avatarViews = new View[12];
        int[] colorViews = {
                R.id.avatar_color_1, R.id.avatar_color_2, R.id.avatar_color_3, R.id.avatar_color_4,
                R.id.avatar_color_5, R.id.avatar_color_6, R.id.avatar_color_7, R.id.avatar_color_8,
                R.id.avatar_color_9, R.id.avatar_color_10, R.id.avatar_color_11, R.id.avatar_color_12
        };

        String nickname = sessionManager.getNickname();
        String firstChar = (nickname != null && !nickname.isEmpty()) ?
                String.valueOf(nickname.charAt(0)).toUpperCase() : "U";

        for (int i = 0; i < 12; i++) {
            avatarViews[i] = dialogView.findViewById(colorViews[i]);
            final String color = AVATAR_COLORS[i];
            avatarViews[i].setOnClickListener(v -> {
                // Create avatar with color and letter
                Bitmap avatar = createAvatarBitmap(color, firstChar);
                String avatarString = bitmapToBase64(avatar);
                sessionManager.setAvatar(avatarString);

                binding.avatarImage.setImageBitmap(avatar);
                binding.avatarImage.setVisibility(View.VISIBLE);
                binding.avatarPlaceholder.setVisibility(View.GONE);

                Toast.makeText(requireContext(), "头像已更换", Toast.LENGTH_SHORT).show();
                currentDialog.dismiss();
            });
        }

        currentDialog.show();
    }

    private Bitmap createAvatarBitmap(String colorHex, String letter) {
        int size = (int) (160 * getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw circular background
        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(Color.parseColor(colorHex));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint);

        // Draw letter
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.4f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Center the text vertically
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = size / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(letter, size / 2f, textY, textPaint);

        return bitmap;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void showEditNicknameDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_text, null);

        EditText editText = dialogView.findViewById(R.id.editText);
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);

        titleText.setText("修改昵称");
        editText.setText(binding.nicknameValue.getText().toString());
        editText.selectAll();

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newNickname = editText.getText().toString().trim();
                    if (!newNickname.isEmpty()) {
                        sessionManager.setNickname(newNickname);
                        binding.nicknameValue.setText(newNickname);
                        // Update avatar letter
                        if (!newNickname.equals("健身爱好者")) {
                            binding.avatarPlaceholder.setText(String.valueOf(newNickname.charAt(0)).toUpperCase());
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showGenderDialog() {
        String[] genders = {"男", "女", "未设置"};
        int currentIndex = 2;
        String currentGender = sessionManager.getGender();
        if (currentGender != null) {
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equals(currentGender)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("选择性别")
                .setSingleChoiceItems(genders, currentIndex, (dialog, which) -> {
                    String selected = genders[which];
                    sessionManager.setGender(selected);
                    binding.genderValue.setText(selected);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditHeightDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_text, null);

        EditText editText = dialogView.findViewById(R.id.editText);
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);

        titleText.setText("修改身高");
        String currentHeight = binding.heightValue.getText().toString().replace(" cm", "");
        if (!currentHeight.equals("未设置")) {
            editText.setText(currentHeight);
            editText.selectAll();
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String heightStr = editText.getText().toString().trim();
                    if (!heightStr.isEmpty()) {
                        try {
                            int height = Integer.parseInt(heightStr);
                            if (height >= 50 && height <= 250) {
                                sessionManager.setHeight(height);
                                binding.heightValue.setText(height + " cm");
                            } else {
                                Toast.makeText(requireContext(), "请输入50-250之间的身高", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "请输入有效数字", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditWeightDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_text, null);

        EditText editText = dialogView.findViewById(R.id.editText);
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);

        titleText.setText("修改体重");
        String currentWeight = binding.weightValue.getText().toString().replace(" kg", "");
        if (!currentWeight.equals("未设置")) {
            editText.setText(currentWeight);
            editText.selectAll();
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String weightStr = editText.getText().toString().trim();
                    if (!weightStr.isEmpty()) {
                        try {
                            float weight = Float.parseFloat(weightStr);
                            if (weight >= 20 && weight <= 300) {
                                sessionManager.setWeight(weight);
                                binding.weightValue.setText(String.format("%.1f kg", weight));
                            } else {
                                Toast.makeText(requireContext(), "请输入20-300之间的体重", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "请输入有效数字", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showGoalDialog() {
        String[] goals = {"增肌", "减脂", "塑形", "保持健康", "未设置"};
        int currentIndex = 4;
        String currentGoal = sessionManager.getFitnessGoal();
        if (currentGoal != null) {
            for (int i = 0; i < goals.length; i++) {
                if (goals[i].equals(currentGoal)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("选择健身目的")
                .setSingleChoiceItems(goals, currentIndex, (dialog, which) -> {
                    String selected = goals[which];
                    sessionManager.setFitnessGoal(selected);
                    binding.goalValue.setText(selected);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
