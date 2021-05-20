package com.mlethe.library.multi.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.mlethe.library.multi.demo.databinding.ActivityMainBinding;
import com.mlethe.library.multi.pay.PayAction;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.wechatPayBtn.setOnClickListener(v -> {

        });
    }
}