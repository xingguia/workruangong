package com.example.myapplication.ui.screens.assessment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentAssessmentBinding;
import com.example.myapplication.util.SessionManager;
import com.example.myapplication.util.UsernameValidator;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AssessmentFragment extends Fragment {

    private FragmentAssessmentBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private int currentStep = 1;
    private final int totalSteps = 5;

    // Form data
    private String selectedGoal = "";
    private String selectedExperience = "";
    private String[] selectedEquipment = new String[0];
    private int[] selectedDays = new int[0];
    private int height = 0;
    private int weight = 0;
    private NestedScrollView scrollView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAssessmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        sessionManager = SessionManager.getInstance(requireContext());
        scrollView = binding.scrollView;

        setupListeners();
        showStep(currentStep);
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                showStep(currentStep);
            } else {
                navController.popBackStack();
            }
        });

        binding.prevBtn.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                showStep(currentStep);
            }
        });

        binding.nextBtn.setOnClickListener(v -> {
            if (canProceed()) {
                if (currentStep < totalSteps) {
                    currentStep++;
                    showStep(currentStep);
                } else {
                    submitAssessment();
                }
            }
        });

        binding.skipBtn.setOnClickListener(v -> {
            showUsernameSetDialog(true);
        });
    }

    private void showUsernameSetDialog(boolean fromSkip) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_set_username, null);

        TextInputLayout usernameInputLayout = dialogView.findViewById(R.id.usernameInputLayout);
        TextInputEditText usernameInput = dialogView.findViewById(R.id.usernameInput);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("确认", null)
                .setNegativeButton("跳过", (d, which) -> {
                    navigateToHome();
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

                UsernameValidator.ValidationResult result = UsernameValidator.validateWithAvailability(requireContext(), username);
                if (!result.valid) {
                    usernameInputLayout.setError(result.errorMessage);
                    return;
                }

                // Save the username
                sessionManager.saveNickname(username);
                sessionManager.addUsernameToSet(username);
                sessionManager.markUsernameSet();

                dialog.dismiss();
                navigateToHome();
            });
        });

        dialog.show();
    }

    private void navigateToHome() {
        sessionManager.markAssessmentCompleted();
        navController.navigate(R.id.action_assessment_to_home);
    }

    private void showStep(int step) {
        binding.stepIndicator.setText(step + "/" + totalSteps);
        binding.progressBar.setProgress(step);
        binding.prevBtn.setVisibility(step > 1 ? View.VISIBLE : View.INVISIBLE);
        binding.nextBtn.setText(step == totalSteps ? getString(R.string.generate_plan) : getString(R.string.next));

        binding.contentContainer.removeAllViews();

        // Scroll to top when changing steps
        if (scrollView != null) {
            scrollView.post(() -> scrollView.scrollTo(0, 0));
        }

        switch (step) {
            case 1:
                showGoalStep();
                break;
            case 2:
                showExperienceStep();
                break;
            case 3:
                showEquipmentStep();
                break;
            case 4:
                showDaysStep();
                break;
            case 5:
                showBodyDataStep();
                break;
        }

        binding.nextBtn.setEnabled(canProceed());
    }

    private void showGoalStep() {
        LinearLayout container = createStepContainer();

        TextView title = createStepTitle(getString(R.string.fitness_goal));
        TextView desc = createStepDesc("选择一个主要目标，我们会为你定制专属计划");
        container.addView(title);
        container.addView(desc);

        LinearLayout grid = new LinearLayout(requireContext());
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        grid.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), 0);

        String[] goals = {getString(R.string.goal_fat_loss), getString(R.string.goal_muscle_gain),
                getString(R.string.goal_shape), getString(R.string.goal_posture)};
        String[] goalKeys = {"fat_loss", "muscle_gain", "shape", "posture"};
        String[] descriptions = {getString(R.string.goal_fat_loss_desc), getString(R.string.goal_muscle_gain_desc),
                getString(R.string.goal_shape_desc), getString(R.string.goal_posture_desc)};

        for (int i = 0; i < goals.length; i++) {
            final String goalKey = goalKeys[i];
            MaterialCardView card = createOptionCard(goals[i], descriptions[i], selectedGoal.equals(goalKey));
            card.setOnClickListener(v -> {
                selectedGoal = goalKey;
                showStep(1);
            });
            grid.addView(card);
        }

        container.addView(grid);
        binding.contentContainer.addView(container);
    }

    private void showExperienceStep() {
        LinearLayout container = createStepContainer();

        TextView title = createStepTitle(getString(R.string.exercise_experience));
        TextView desc = createStepDesc("帮助我们了解你的基础，制定合适的强度");
        container.addView(title);
        container.addView(desc);

        LinearLayout list = new LinearLayout(requireContext());
        list.setOrientation(LinearLayout.VERTICAL);
        list.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        list.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), 0);

        String[] exps = {getString(R.string.exp_beginner), getString(R.string.exp_intermediate),
                getString(R.string.exp_advanced)};
        String[] expKeys = {"beginner", "intermediate", "advanced"};
        String[] expDescs = {getString(R.string.exp_beginner_desc), getString(R.string.exp_intermediate_desc),
                getString(R.string.exp_advanced_desc)};

        for (int i = 0; i < exps.length; i++) {
            final String expKey = expKeys[i];
            MaterialCardView card = createOptionCard(exps[i], expDescs[i], selectedExperience.equals(expKey));
            card.setOnClickListener(v -> {
                selectedExperience = expKey;
                showStep(2);
            });
            list.addView(card);
        }

        container.addView(list);
        binding.contentContainer.addView(container);
    }

    private void showEquipmentStep() {
        LinearLayout container = createStepContainer();

        TextView title = createStepTitle(getString(R.string.equipment_available));
        TextView desc = createStepDesc("可多选，我们会为你选择合适的动作");
        container.addView(title);
        container.addView(desc);

        LinearLayout grid = new LinearLayout(requireContext());
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        grid.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), 0);

        String[] equip = {getString(R.string.equip_none), getString(R.string.equip_dumbbell),
                getString(R.string.equip_barbell), getString(R.string.equip_band),
                getString(R.string.equip_gym), getString(R.string.equip_kettlebell)};

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < equip.length; i++) {
            if (i > 0 && i % 3 == 0) {
                grid.addView(row);
                row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            final int index = i;
            MaterialCardView card = createSmallOptionCard(equip[i], false);
            card.setOnClickListener(v -> {
                // Toggle equipment selection
                showStep(3);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(0, 0, dpToPx(8), dpToPx(8));
            card.setLayoutParams(params);
            row.addView(card);
        }

        grid.addView(row);
        container.addView(grid);
        binding.contentContainer.addView(container);
    }

    private void showDaysStep() {
        LinearLayout container = createStepContainer();

        TextView title = createStepTitle(getString(R.string.weekly_days));
        TextView desc = createStepDesc("我们会根据你的时间安排训练计划");
        container.addView(title);
        container.addView(desc);

        LinearLayout daysContainer = new LinearLayout(requireContext());
        daysContainer.setOrientation(LinearLayout.HORIZONTAL);
        daysContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        daysContainer.setGravity(android.view.Gravity.CENTER);
        daysContainer.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), 0);

        for (int i = 1; i <= 7; i++) {
            final int day = i;
            TextView dayBtn = new TextView(requireContext());
            dayBtn.setText(String.valueOf(i));
            dayBtn.setTextSize(15);
            dayBtn.setGravity(android.view.Gravity.CENTER);
            dayBtn.setBackgroundResource(R.drawable.bg_card);
            dayBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(44), dpToPx(44));
            params.setMargins(0, 0, dpToPx(12), 0);
            dayBtn.setLayoutParams(params);

            dayBtn.setOnClickListener(v -> {
                // Toggle day selection
            });

            daysContainer.addView(dayBtn);
        }

        container.addView(daysContainer);

        // Summary
        TextView summary = new TextView(requireContext());
        summary.setText(getString(R.string.days_selected, 0));
        summary.setTextSize(14);
        summary.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        summary.setGravity(android.view.Gravity.CENTER);
        summary.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));
        summary.setPadding(0, dpToPx(20), 0, dpToPx(16));

        container.addView(summary);
        binding.contentContainer.addView(container);
    }

    private void showBodyDataStep() {
        LinearLayout container = createStepContainer();

        TextView title = createStepTitle(getString(R.string.body_measurement));
        TextView desc = createStepDesc("用于计算消耗和追踪进步");
        container.addView(title);
        container.addView(desc);

        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        form.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), 0);

        // Height input
        com.google.android.material.textfield.TextInputLayout heightLayout =
                new com.google.android.material.textfield.TextInputLayout(requireContext(),
                        null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        heightLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        heightLayout.setHint(getString(R.string.height) + " (cm)");

        com.google.android.material.textfield.TextInputEditText heightInput =
                new com.google.android.material.textfield.TextInputEditText(heightLayout.getContext());
        heightInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        heightLayout.addView(heightInput);
        form.addView(heightLayout);

        // Weight input
        com.google.android.material.textfield.TextInputLayout weightLayout =
                new com.google.android.material.textfield.TextInputLayout(requireContext(),
                        null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        weightLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        weightLayout.setHint(getString(R.string.weight) + " (kg)");

        com.google.android.material.textfield.TextInputEditText weightInput =
                new com.google.android.material.textfield.TextInputEditText(weightLayout.getContext());
        weightInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        weightLayout.addView(weightInput);
        form.addView(weightLayout);

        container.addView(form);
        binding.contentContainer.addView(container);
    }

    private LinearLayout createStepContainer() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return container;
    }

    private TextView createStepTitle(String text) {
        TextView title = new TextView(requireContext());
        title.setText(text);
        title.setTextSize(24);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setGravity(android.view.Gravity.CENTER);
        title.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));
        title.setPadding(0, dpToPx(24), 0, dpToPx(8));
        return title;
    }

    private TextView createStepDesc(String text) {
        TextView desc = new TextView(requireContext());
        desc.setText(text);
        desc.setTextSize(14);
        desc.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        desc.setGravity(android.view.Gravity.CENTER);
        desc.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));
        return desc;
    }

    private MaterialCardView createOptionCard(String title, String desc, boolean selected) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.bg_card : R.color.bg_card));
        card.setStrokeColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.primary : R.color.border_color));
        card.setStrokeWidth(selected ? dpToPx(2) : dpToPx(1));
        card.setRadius(dpToPx(16));
        card.setCardElevation(0);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16));

        TextView titleView = new TextView(requireContext());
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        TextView descView = new TextView(requireContext());
        descView.setText(desc);
        descView.setTextSize(12);
        descView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_muted));

        content.addView(titleView);
        content.addView(descView);
        card.addView(content);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(params);

        return card;
    }

    private MaterialCardView createSmallOptionCard(String title, boolean selected) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.primary_20 : R.color.bg_card));
        card.setStrokeColor(ContextCompat.getColor(requireContext(),
                selected ? R.color.primary : R.color.border_color));
        card.setStrokeWidth(selected ? dpToPx(2) : dpToPx(1));
        card.setRadius(dpToPx(16));
        card.setCardElevation(0);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(android.view.Gravity.CENTER);
        content.setPadding(dpToPx(16), dpToPx(20), dpToPx(16), dpToPx(20));

        TextView titleView = new TextView(requireContext());
        titleView.setText(title);
        titleView.setTextSize(13);
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        content.addView(titleView);
        card.addView(content);

        return card;
    }

    private boolean canProceed() {
        switch (currentStep) {
            case 1:
                return !selectedGoal.isEmpty();
            case 2:
                return !selectedExperience.isEmpty();
            case 3:
                return selectedEquipment.length > 0;
            case 4:
                return selectedDays.length > 0;
            case 5:
                return height > 0 && weight > 0;
            default:
                return false;
        }
    }

    private void submitAssessment() {
        // Save assessment data
        sessionManager.saveBodyData(height, (float) weight);
        showUsernameSetDialog(false);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
