package com.example.myapplication.ui.screens.progress;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentProgressBinding;
import com.example.myapplication.model.BodyRecord;
import com.example.myapplication.util.RecordManager;
import com.example.myapplication.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ProgressFragment extends Fragment {

    private FragmentProgressBinding binding;
    private SessionManager sessionManager;
    private RecordManager recordManager;
    private String currentFilter = "week";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProgressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = SessionManager.getInstance(requireContext());
        recordManager = RecordManager.getInstance(requireContext());

        setupInitialData();
        setupListeners();
        updateFilter("week");
    }

    private void setupInitialData() {
        // 先清空所有显示的值
        binding.heightValue.setText("--");
        binding.weightValue.setText("--");
        binding.bodyFatValue.setText("--");
        binding.waistValue.setText("--");
        binding.hipValue.setText("--");
        binding.bmiValue.setText("--");

        // Display saved height and weight
        int height = sessionManager.getHeight();
        int initialHeight = sessionManager.getInitialHeight();
        float weight = sessionManager.getWeight();
        float initialWeight = sessionManager.getInitialWeight();
        float bodyFat = sessionManager.getBodyFat();
        float initialBodyFat = sessionManager.getInitialBodyFat();
        float waist = sessionManager.getWaist();
        float initialWaist = sessionManager.getInitialWaist();
        float hip = sessionManager.getHip();
        float initialHip = sessionManager.getInitialHip();

        // 身高：优先显示当前值，其次初始值
        if (height > 0) {
            binding.heightValue.setText(String.valueOf(height));
        } else if (initialHeight > 0) {
            binding.heightValue.setText(String.valueOf(initialHeight));
        }

        // 体重：优先显示当前值，其次初始值
        if (weight > 0) {
            binding.weightValue.setText(String.format("%.1f", weight));
        } else if (initialWeight > 0) {
            binding.weightValue.setText(String.format("%.1f", initialWeight));
        }

        // 体脂率
        if (bodyFat > 0) {
            binding.bodyFatValue.setText(String.format("%.1f", bodyFat));
        } else if (initialBodyFat > 0) {
            binding.bodyFatValue.setText(String.format("%.1f", initialBodyFat));
        }

        // 腰围
        if (waist > 0) {
            binding.waistValue.setText(String.format("%.1f", waist));
        } else if (initialWaist > 0) {
            binding.waistValue.setText(String.format("%.1f", initialWaist));
        }

        // 臀围
        if (hip > 0) {
            binding.hipValue.setText(String.format("%.1f", hip));
        } else if (initialHip > 0) {
            binding.hipValue.setText(String.format("%.1f", initialHip));
        }

        // 计算并显示BMI
        float displayHeight = height > 0 ? height : initialHeight;
        float displayWeight = weight > 0 ? weight : initialWeight;
        if (displayHeight > 0 && displayWeight > 0) {
            float bmi = calculateBMI(displayWeight, displayHeight);
            binding.bmiValue.setText(String.format("%.1f", bmi));
            updateBMIIndicator(bmi);
        }

        // 计算体重变化
        if (initialWeight > 0 && weight > 0) {
            float change = weight - initialWeight;
            if (binding.weightChange != null) {
                binding.weightChange.setVisibility(View.VISIBLE);
                if (change > 0) {
                    binding.weightChange.setText(String.format("+%.1fkg", change));
                    binding.weightChange.setTextColor(getResources().getColor(R.color.error, null));
                } else if (change < 0) {
                    binding.weightChange.setText(String.format("%.1fkg", change));
                    binding.weightChange.setTextColor(getResources().getColor(R.color.success, null));
                } else {
                    binding.weightChange.setText("0.0kg");
                    binding.weightChange.setTextColor(getResources().getColor(R.color.text_muted, null));
                }
            }
        } else if (binding.weightChange != null) {
            binding.weightChange.setVisibility(View.GONE);
        }

        // Update empty state visibility
        boolean hasData = (height > 0 || initialHeight > 0) && (weight > 0 || initialWeight > 0);
        binding.emptyState.setVisibility(hasData ? View.GONE : View.VISIBLE);

        // Update record count
        int recordCount = recordManager.getRecordCount();
        binding.recordCount.setText(String.format("共%d条", recordCount));

        // Update chart and records
        updateChart();
        updateRecordsList();
    }

    private float calculateBMI(float weightKg, float heightCm) {
        float heightM = heightCm / 100f;
        return weightKg / (heightM * heightM);
    }

    private void updateBMIIndicator(float bmi) {
        if (binding.bmiBarContainer == null || binding.bmiPointer == null) return;

        binding.bmiPointer.setVisibility(View.VISIBLE);

        // 需要等待视图完全测量完成后再计算位置
        binding.bmiBarContainer.post(() -> {
            int containerWidth = binding.bmiBarContainer.getWidth();
            if (containerWidth == 0) return;

            // Calculate position on the bar (0-1)
            float position;
            int color;

            if (bmi < 18.5f) {
                position = (bmi / 18.5f) * 0.25f;
                color = ContextCompat.getColor(requireContext(), R.color.info);
                binding.bmiStatus.setText("偏瘦");
                binding.bmiStatus.setTextColor(color);
            } else if (bmi < 24f) {
                position = 0.25f + ((bmi - 18.5f) / (24f - 18.5f)) * 0.25f;
                color = ContextCompat.getColor(requireContext(), R.color.success);
                binding.bmiStatus.setText("正常");
                binding.bmiStatus.setTextColor(color);
            } else if (bmi < 28f) {
                position = 0.5f + ((bmi - 24f) / (28f - 24f)) * 0.25f;
                color = ContextCompat.getColor(requireContext(), R.color.warning);
                binding.bmiStatus.setText("偏胖");
                binding.bmiStatus.setTextColor(color);
            } else {
                position = 0.75f + Math.min((bmi - 28f) / (32f - 28f), 1f) * 0.25f;
                color = ContextCompat.getColor(requireContext(), R.color.error);
                binding.bmiStatus.setText("肥胖");
                binding.bmiStatus.setTextColor(color);
            }

            // Calculate pixel position - pointer center should point to the position
            float density = getResources().getDisplayMetrics().density;
            int pointerWidth = (int) (16 * density);
            float pointerX = position * containerWidth;
            float pointerLeftMargin = pointerX - (pointerWidth / 2f);

            // Create new LayoutParams with proper margins
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(pointerWidth, (int) (16 * density));
            params.topMargin = 0;
            params.leftMargin = (int) pointerLeftMargin;
            binding.bmiPointer.setLayoutParams(params);

            // Set pointer color
            binding.bmiPointer.setColorFilter(color);

            // Update BMI value color
            binding.bmiValue.setTextColor(color);
        });
    }

    private void setupListeners() {
        // Add record button - show dialog to add new body data
        binding.addRecordBtn.setOnClickListener(v -> showAddRecordDialog());

        // Initial data button - show dialog to set initial data
        binding.initialDataBtn.setOnClickListener(v -> showInitialDataDialog());

        // Filter buttons
        binding.filterWeek.setOnClickListener(v -> updateFilter("week"));
        binding.filterMonth.setOnClickListener(v -> updateFilter("month"));
        binding.filterQuarter.setOnClickListener(v -> updateFilter("quarter"));
    }

    private void updateFilter(String period) {
        currentFilter = period;

        binding.filterWeek.setBackground(null);
        binding.filterMonth.setBackground(null);
        binding.filterQuarter.setBackground(null);

        binding.filterWeek.setTextColor(getResources().getColor(R.color.text_muted, null));
        binding.filterMonth.setTextColor(getResources().getColor(R.color.text_muted, null));
        binding.filterQuarter.setTextColor(getResources().getColor(R.color.text_muted, null));

        switch (period) {
            case "week":
                binding.filterWeek.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_primary));
                binding.filterWeek.setTextColor(getResources().getColor(R.color.white, null));
                break;
            case "month":
                binding.filterMonth.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_primary));
                binding.filterMonth.setTextColor(getResources().getColor(R.color.white, null));
                break;
            case "quarter":
                binding.filterQuarter.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_gradient_primary));
                binding.filterQuarter.setTextColor(getResources().getColor(R.color.white, null));
                break;
        }

        updateChart();
    }

    private void updateChart() {
        // Get records based on current filter
        int days;
        switch (currentFilter) {
            case "week":
                days = 7;
                break;
            case "month":
                days = 30;
                break;
            case "quarter":
                days = 90;
                break;
            default:
                days = 7;
        }

        List<BodyRecord> records = recordManager.getRecordsByPeriod(days);

        if (records.isEmpty()) {
            binding.chartEmptyState.setVisibility(View.VISIBLE);
            binding.bodyChartView.setVisibility(View.GONE);
        } else {
            binding.chartEmptyState.setVisibility(View.GONE);
            binding.bodyChartView.setVisibility(View.VISIBLE);

            // Reverse to chronological order for chart
            List<BodyRecord> chartRecords = new ArrayList<>(records);
            java.util.Collections.reverse(chartRecords);

            List<Float> weights = new ArrayList<>();
            List<Float> bmis = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            for (BodyRecord record : chartRecords) {
                weights.add(record.getWeight());
                bmis.add(record.getBmi());
                labels.add(record.getShortDate());
            }

            binding.bodyChartView.setData(weights, bmis, labels);
        }
    }

    private void updateRecordsList() {
        binding.recordsContainer.removeAllViews();

        List<BodyRecord> records = recordManager.getRecords();

        if (records.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recordsContainer.setVisibility(View.GONE);
            return;
        }

        binding.emptyState.setVisibility(View.GONE);
        binding.recordsContainer.setVisibility(View.VISIBLE);

        float initialWeight = sessionManager.getInitialWeight();

        for (BodyRecord record : records) {
            View recordView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_body_record, binding.recordsContainer, false);

            TextView dateText = recordView.findViewById(R.id.recordDate);
            TextView dayOfWeek = recordView.findViewById(R.id.recordDayOfWeek);
            TextView weightText = recordView.findViewById(R.id.recordWeight);
            TextView weightChange = recordView.findViewById(R.id.recordWeightChange);
            TextView bmiText = recordView.findViewById(R.id.recordBmi);
            TextView bodyFatText = recordView.findViewById(R.id.recordBodyFat);
            TextView waistText = recordView.findViewById(R.id.recordWaist);

            dateText.setText(record.getFormattedDate());
            dayOfWeek.setText(record.getDayOfWeek());
            weightText.setText(String.format("%.1f", record.getWeight()));
            bmiText.setText(String.format("%.1f", record.getBmi()));

            // Calculate weight change from initial
            if (initialWeight > 0) {
                float change = record.getWeight() - initialWeight;
                if (change > 0) {
                    weightChange.setText(String.format("+%.1fkg", change));
                    weightChange.setTextColor(getResources().getColor(R.color.error, null));
                } else if (change < 0) {
                    weightChange.setText(String.format("%.1fkg", change));
                    weightChange.setTextColor(getResources().getColor(R.color.success, null));
                } else {
                    weightChange.setText("0.0kg");
                    weightChange.setTextColor(getResources().getColor(R.color.text_muted, null));
                }
            } else {
                weightChange.setVisibility(View.GONE);
            }

            // Body fat
            if (record.getBodyFat() > 0) {
                bodyFatText.setText(String.format("%.1f", record.getBodyFat()));
            } else {
                bodyFatText.setText("--");
                bodyFatText.setTextColor(getResources().getColor(R.color.text_muted, null));
            }

            // Waist
            if (record.getWaist() > 0) {
                waistText.setText(String.format("%.0f", record.getWaist()));
            } else {
                waistText.setText("--");
                waistText.setTextColor(getResources().getColor(R.color.text_muted, null));
            }

            binding.recordsContainer.addView(recordView);
        }
    }

    private void showAddRecordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_body_data, null);

        com.google.android.material.textfield.TextInputEditText heightInput = dialogView.findViewById(R.id.heightInput);
        com.google.android.material.textfield.TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        com.google.android.material.textfield.TextInputEditText bodyFatInput = dialogView.findViewById(R.id.bodyFatInput);
        com.google.android.material.textfield.TextInputEditText waistInput = dialogView.findViewById(R.id.waistInput);
        com.google.android.material.textfield.TextInputEditText hipInput = dialogView.findViewById(R.id.hipInput);
        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView subtitle = dialogView.findViewById(R.id.dialogSubtitle);

        title.setText("添加记录");
        title.setTextColor(getResources().getColor(R.color.text_primary, null));
        subtitle.setText("记录今天的身体数据，追踪你的变化");
        subtitle.setTextColor(getResources().getColor(R.color.text_secondary, null));

        // 设置输入框文字颜色为黑色
        setInputTextColorBlack(heightInput);
        setInputTextColorBlack(weightInput);
        setInputTextColorBlack(bodyFatInput);
        setInputTextColorBlack(waistInput);
        setInputTextColorBlack(hipInput);

        // Pre-fill with current values
        int currentHeight = sessionManager.getHeight();
        float currentWeight = sessionManager.getWeight();
        float currentBodyFat = sessionManager.getBodyFat();
        float currentWaist = sessionManager.getWaist();
        float currentHip = sessionManager.getHip();

        if (currentHeight > 0) {
            heightInput.setText(String.valueOf(currentHeight));
        }
        if (currentWeight > 0) {
            weightInput.setText(String.format("%.1f", currentWeight));
        }
        if (currentBodyFat > 0) {
            bodyFatInput.setText(String.format("%.1f", currentBodyFat));
        }
        if (currentWaist > 0) {
            waistInput.setText(String.format("%.1f", currentWaist));
        }
        if (currentHip > 0) {
            hipInput.setText(String.format("%.1f", currentHip));
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();

        // 设置对话框背景为白色圆角
        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.dialog_background);
                // 设置按钮文字颜色
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary, null));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.text_muted, null));
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String heightStr = heightInput.getText() != null ? heightInput.getText().toString().trim() : "";
                String weightStr = weightInput.getText() != null ? weightInput.getText().toString().trim() : "";
                String bodyFatStr = bodyFatInput.getText() != null ? bodyFatInput.getText().toString().trim() : "";
                String waistStr = waistInput.getText() != null ? waistInput.getText().toString().trim() : "";
                String hipStr = hipInput.getText() != null ? hipInput.getText().toString().trim() : "";

                int height = 0;
                float weight = 0;
                float bodyFat = 0;
                float waist = 0;
                float hip = 0;
                boolean valid = true;

                if (heightStr.isEmpty()) {
                    heightInput.setError("请输入身高");
                    valid = false;
                } else {
                    try {
                        height = Integer.parseInt(heightStr);
                        if (height < 50 || height > 250) {
                            heightInput.setError("请输入50-250之间的身高");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        heightInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                if (weightStr.isEmpty()) {
                    weightInput.setError("请输入体重");
                    valid = false;
                } else {
                    try {
                        weight = Float.parseFloat(weightStr);
                        if (weight < 20 || weight > 300) {
                            weightInput.setError("请输入20-300之间的体重");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        weightInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                // Optional fields validation
                if (!bodyFatStr.isEmpty()) {
                    try {
                        bodyFat = Float.parseFloat(bodyFatStr);
                        if (bodyFat < 0 || bodyFat > 100) {
                            bodyFatInput.setError("请输入0-100之间的数值");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        bodyFatInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                if (!waistStr.isEmpty()) {
                    try {
                        waist = Float.parseFloat(waistStr);
                        if (waist < 40 || waist > 200) {
                            waistInput.setError("请输入40-200之间的数值");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        waistInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                if (!hipStr.isEmpty()) {
                    try {
                        hip = Float.parseFloat(hipStr);
                        if (hip < 40 || hip > 200) {
                            hipInput.setError("请输入40-200之间的数值");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        hipInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                if (valid) {
                    // 保存当前数据
                    sessionManager.saveFullBodyData(height, weight, bodyFat, waist, hip);

                    // 保存到记录列表
                    BodyRecord record = new BodyRecord(height, weight, bodyFat, waist, hip);
                    recordManager.saveRecord(record);

                    dialog.dismiss();
                    // 延迟刷新UI确保对话框已关闭
                    binding.getRoot().post(() -> {
                        setupInitialData();
                        updateChart();
                    });
                }
            });
        });

        dialog.show();
    }

    private void showInitialDataDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_body_data, null);

        com.google.android.material.textfield.TextInputEditText heightInput = dialogView.findViewById(R.id.heightInput);
        com.google.android.material.textfield.TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        com.google.android.material.textfield.TextInputEditText bodyFatInput = dialogView.findViewById(R.id.bodyFatInput);
        com.google.android.material.textfield.TextInputEditText waistInput = dialogView.findViewById(R.id.waistInput);
        com.google.android.material.textfield.TextInputEditText hipInput = dialogView.findViewById(R.id.hipInput);
        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView subtitle = dialogView.findViewById(R.id.dialogSubtitle);

        title.setText("设置最初数据");
        title.setTextColor(getResources().getColor(R.color.text_primary, null));
        subtitle.setText("作为基准线，记录你的起点");
        subtitle.setTextColor(getResources().getColor(R.color.text_secondary, null));

        // 设置输入框文字颜色为黑色
        setInputTextColorBlack(heightInput);
        setInputTextColorBlack(weightInput);
        setInputTextColorBlack(bodyFatInput);
        setInputTextColorBlack(waistInput);
        setInputTextColorBlack(hipInput);

        // Pre-fill with current initial data
        int initialHeight = sessionManager.getInitialHeight();
        float initialWeight = sessionManager.getInitialWeight();
        float initialBodyFat = sessionManager.getInitialBodyFat();
        float initialWaist = sessionManager.getInitialWaist();
        float initialHip = sessionManager.getInitialHip();

        if (initialHeight > 0) {
            heightInput.setText(String.valueOf(initialHeight));
        }
        if (initialWeight > 0) {
            weightInput.setText(String.format("%.1f", initialWeight));
        }
        if (initialBodyFat > 0) {
            bodyFatInput.setText(String.format("%.1f", initialBodyFat));
        }
        if (initialWaist > 0) {
            waistInput.setText(String.format("%.1f", initialWaist));
        }
        if (initialHip > 0) {
            hipInput.setText(String.format("%.1f", initialHip));
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();

        // 设置对话框背景为白色圆角
        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(R.drawable.dialog_background);
                // 设置按钮文字颜色
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary, null));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.text_muted, null));
            }
        });

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String heightStr = heightInput.getText() != null ? heightInput.getText().toString().trim() : "";
                String weightStr = weightInput.getText() != null ? weightInput.getText().toString().trim() : "";
                String bodyFatStr = bodyFatInput.getText() != null ? bodyFatInput.getText().toString().trim() : "";
                String waistStr = waistInput.getText() != null ? waistInput.getText().toString().trim() : "";
                String hipStr = hipInput.getText() != null ? hipInput.getText().toString().trim() : "";

                int height = 0;
                float weight = 0;
                float bodyFat = 0;
                float waist = 0;
                float hip = 0;
                boolean valid = true;

                if (heightStr.isEmpty()) {
                    heightInput.setError("请输入身高");
                    valid = false;
                } else {
                    try {
                        height = Integer.parseInt(heightStr);
                        if (height < 50 || height > 250) {
                            heightInput.setError("请输入50-250之间的身高");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        heightInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                if (weightStr.isEmpty()) {
                    weightInput.setError("请输入体重");
                    valid = false;
                } else {
                    try {
                        weight = Float.parseFloat(weightStr);
                        if (weight < 20 || weight > 300) {
                            weightInput.setError("请输入20-300之间的体重");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        weightInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                // Optional fields validation
                if (!bodyFatStr.isEmpty()) {
                    try {
                        bodyFat = Float.parseFloat(bodyFatStr);
                        if (bodyFat < 0 || bodyFat > 100) {
                            bodyFatInput.setError("请输入0-100之间的数值");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        bodyFatInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                if (!waistStr.isEmpty()) {
                    try {
                        waist = Float.parseFloat(waistStr);
                        if (waist < 40 || waist > 200) {
                            waistInput.setError("请输入40-200之间的数值");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        waistInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                if (!hipStr.isEmpty()) {
                    try {
                        hip = Float.parseFloat(hipStr);
                        if (hip < 40 || hip > 200) {
                            hipInput.setError("请输入40-200之间的数值");
                            valid = false;
                        }
                    } catch (NumberFormatException e) {
                        hipInput.setError("请输入有效数字");
                        valid = false;
                    }
                }

                if (valid) {
                    // 同时保存初始数据和当前数据
                    sessionManager.saveInitialBodyData(height, weight, bodyFat, waist, hip);
                    sessionManager.saveFullBodyData(height, weight, bodyFat, waist, hip);

                    // 保存到记录列表
                    BodyRecord record = new BodyRecord(height, weight, bodyFat, waist, hip);
                    recordManager.saveRecord(record);

                    dialog.dismiss();
                    // 延迟刷新UI确保对话框已关闭
                    binding.getRoot().post(() -> {
                        setupInitialData();
                        updateChart();
                    });
                }
            });
        });

        dialog.show();
    }

    private void setInputTextColorBlack(TextView textView) {
        if (textView != null) {
            textView.setTextColor(getResources().getColor(R.color.black, null));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupInitialData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
