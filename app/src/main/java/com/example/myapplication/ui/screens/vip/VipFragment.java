package com.example.myapplication.ui.screens.vip;

import android.os.Bundle;
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
        setupListeners();
        setupPlanSelection();
        updateSelectedPlan();
    }

    private void setupVipStatus() {
        boolean isVip = sessionManager.isVip();
        if (isVip) {
            binding.vipHeroSection.setVisibility(View.GONE);
            binding.vipStatusSection.setVisibility(View.VISIBLE);

            String expireTime = sessionManager.getVipExpireTime();
            if (expireTime != null) {
                binding.vipExpiry.setText("有效期至 " + expireTime.split("T")[0]);
            }
        } else {
            binding.vipHeroSection.setVisibility(View.VISIBLE);
            binding.vipStatusSection.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // 返回按钮
        binding.backBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });

        // 续费按钮
        binding.renewBtn.setOnClickListener(v -> {
            binding.vipHeroSection.setVisibility(View.VISIBLE);
            binding.vipStatusSection.setVisibility(View.GONE);
        });

        // 订阅按钮
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
        // 重置所有卡片样式
        resetPlanCardStyles();

        // 设置选中状态
        int selectedColor = getResources().getColor(R.color.primary, null);
        float density = getResources().getDisplayMetrics().density;

        switch (selectedPlan) {
            case "month":
                binding.planMonth.setStrokeColor(selectedColor);
                binding.planMonth.setStrokeWidth((int) (2 * density));
                binding.priceValue.setText("39");
                binding.priceUnit.setText("/月");
                binding.originalPrice.setVisibility(View.GONE);
                break;
            case "quarter":
                binding.planQuarter.setStrokeColor(selectedColor);
                binding.planQuarter.setStrokeWidth((int) (2 * density));
                binding.priceValue.setText("99");
                binding.priceUnit.setText("/季");
                binding.originalPrice.setVisibility(View.VISIBLE);
                binding.originalPrice.setText("原价 ¥117");
                break;
            case "year":
                binding.planYear.setStrokeColor(selectedColor);
                binding.planYear.setStrokeWidth((int) (2 * density));
                binding.priceValue.setText("299");
                binding.priceUnit.setText("/年");
                binding.originalPrice.setVisibility(View.VISIBLE);
                binding.originalPrice.setText("原价 ¥468");
                break;
        }
    }

    private void resetPlanCardStyles() {
        int defaultColor = getResources().getColor(R.color.border_color, null);
        float density = getResources().getDisplayMetrics().density;

        binding.planMonth.setStrokeColor(defaultColor);
        binding.planMonth.setStrokeWidth(0);
        binding.planQuarter.setStrokeColor(defaultColor);
        binding.planQuarter.setStrokeWidth(0);
        binding.planYear.setStrokeColor(defaultColor);
        binding.planYear.setStrokeWidth(0);
    }

    private void handleSubscribe() {
        String price = binding.priceValue.getText().toString();
        Toast.makeText(requireContext(), "支付功能开发中", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
