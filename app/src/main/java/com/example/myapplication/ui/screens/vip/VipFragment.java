package com.example.myapplication.ui.screens.vip;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentVipBinding;
import com.example.myapplication.util.SessionManager;

public class VipFragment extends Fragment {

    private FragmentVipBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private String selectedPlan = "quarter";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVipBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        sessionManager = SessionManager.getInstance(requireContext());

        setupVipStatus();
        setupFeatureList();
        setupListeners();
        setupPlanSelection();
        setupFAQ();
        updateSelectedPlan();
    }

    private void setupVipStatus() {
        boolean isVip = sessionManager.isVip();
        if (isVip) {
            binding.vipHeroSection.setVisibility(View.GONE);
            binding.vipStatusSection.setVisibility(View.VISIBLE);

            String expireTime = sessionManager.getVipExpireTime();
            if (expireTime != null && !expireTime.isEmpty()) {
                String displayDate = expireTime.split("T")[0].split(" ")[0];
                binding.vipExpiry.setText("有效期至 " + displayDate);
            }
        } else {
            binding.vipHeroSection.setVisibility(View.VISIBLE);
            binding.vipStatusSection.setVisibility(View.GONE);
        }
    }

    private void setupFeatureList() {
        // 设置VIP功能列表的文本
        int[] featureIds = {R.id.feature1, R.id.feature2, R.id.feature3, R.id.feature4};
        int[] featureTexts = {
                R.string.vip_feature_personal,
                R.string.vip_feature_ai,
                R.string.vip_feature_coach,
                R.string.vip_feature_library
        };

        for (int i = 0; i < featureIds.length; i++) {
            View featureView = binding.vipHeroSection.findViewById(featureIds[i]);
            if (featureView != null) {
                TextView textView = featureView.findViewById(R.id.featureText);
                if (textView != null) {
                    textView.setText(featureTexts[i]);
                }
            }
        }
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });

        binding.renewBtn.setOnClickListener(v -> {
            binding.vipHeroSection.setVisibility(View.VISIBLE);
            binding.vipStatusSection.setVisibility(View.GONE);
        });

        binding.subscribeBtn.setOnClickListener(v -> {
            handleSubscribe();
        });
    }

    private void setupPlanSelection() {
        binding.planMonth.setOnClickListener(v -> selectPlan("month"));
        binding.planQuarter.setOnClickListener(v -> selectPlan("quarter"));
        binding.planYear.setOnClickListener(v -> selectPlan("year"));
    }

    private void selectPlan(String plan) {
        selectedPlan = plan;
        updateSelectedPlan();
    }

    private void updateSelectedPlan() {
        resetPlanCardStyles();

        int selectedColor = getResources().getColor(R.color.primary, null);
        float density = getResources().getDisplayMetrics().density;

        switch (selectedPlan) {
            case "month":
                binding.planMonth.setStrokeColor(selectedColor);
                binding.planMonth.setStrokeWidth((int) (2 * density));
                binding.finalPrice.setText("39");
                break;
            case "quarter":
                binding.planQuarter.setStrokeColor(selectedColor);
                binding.planQuarter.setStrokeWidth((int) (2 * density));
                binding.finalPrice.setText("99");
                break;
            case "year":
                binding.planYear.setStrokeColor(selectedColor);
                binding.planYear.setStrokeWidth((int) (2 * density));
                binding.finalPrice.setText("299");
                break;
        }
    }

    private void resetPlanCardStyles() {
        int defaultColor = getResources().getColor(R.color.border_color, null);

        binding.planMonth.setStrokeColor(defaultColor);
        binding.planMonth.setStrokeWidth(0);
        binding.planQuarter.setStrokeColor(defaultColor);
        binding.planQuarter.setStrokeWidth(0);
        binding.planYear.setStrokeColor(defaultColor);
        binding.planYear.setStrokeWidth(0);
    }

    private void setupFAQ() {
        // FAQ 1: 会员可以退款吗？
        LinearLayout faqItem1 = binding.getRoot().findViewById(R.id.faqItem1);
        TextView faqAnswer1 = binding.getRoot().findViewById(R.id.faqAnswer1);
        ImageView faqArrow1 = binding.getRoot().findViewById(R.id.faqArrow1);

        if (faqItem1 != null && faqAnswer1 != null) {
            faqItem1.setOnClickListener(v -> {
                if (faqAnswer1.getVisibility() == View.GONE) {
                    faqAnswer1.setVisibility(View.VISIBLE);
                    if (faqArrow1 != null) {
                        ObjectAnimator.ofFloat(faqArrow1, "rotation", 0f, 180f).setDuration(200).start();
                    }
                } else {
                    faqAnswer1.setVisibility(View.GONE);
                    if (faqArrow1 != null) {
                        ObjectAnimator.ofFloat(faqArrow1, "rotation", 180f, 0f).setDuration(200).start();
                    }
                }
            });
        }

        // FAQ 2: 如何取消自动续费？
        LinearLayout faqItem2 = binding.getRoot().findViewById(R.id.faqItem2);
        TextView faqAnswer2 = binding.getRoot().findViewById(R.id.faqAnswer2);
        ImageView faqArrow2 = binding.getRoot().findViewById(R.id.faqArrow2);

        if (faqItem2 != null && faqAnswer2 != null) {
            faqItem2.setOnClickListener(v -> {
                if (faqAnswer2.getVisibility() == View.GONE) {
                    faqAnswer2.setVisibility(View.VISIBLE);
                    if (faqArrow2 != null) {
                        ObjectAnimator.ofFloat(faqArrow2, "rotation", 0f, 180f).setDuration(200).start();
                    }
                } else {
                    faqAnswer2.setVisibility(View.GONE);
                    if (faqArrow2 != null) {
                        ObjectAnimator.ofFloat(faqArrow2, "rotation", 180f, 0f).setDuration(200).start();
                    }
                }
            });
        }
    }

    private void handleSubscribe() {
        String price = binding.finalPrice.getText().toString();
        Toast.makeText(requireContext(), "支付功能开发中", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
