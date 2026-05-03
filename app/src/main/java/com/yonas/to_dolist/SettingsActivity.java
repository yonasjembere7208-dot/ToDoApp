package com.yonas.to_dolist;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.yonas.to_dolist.Utils.DataBaseHelper;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup themeRadioGroup;
    private MaterialButton btnClearData, btnClearCompleted;
    private DataBaseHelper myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        btnClearData = findViewById(R.id.btnClearData);
        btnClearCompleted = findViewById(R.id.btnClearCompleted);
        myDB = new DataBaseHelper(this);

        setupThemeSelection();
        setupDataManagement();
    }

    private void setupThemeSelection() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeRadioGroup.check(R.id.radioDark);
        } else if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            themeRadioGroup.check(R.id.radioLight);
        } else {
            themeRadioGroup.check(R.id.radioSystem);
        }

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.radioDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });
    }

    private void setupDataManagement() {
        btnClearCompleted.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Completed")
                    .setMessage("Remove all tasks that are marked as finished?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        myDB.deleteCompletedTasks();
                        Toast.makeText(SettingsActivity.this, "Completed tasks cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        btnClearData.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.clear_all_tasks)
                    .setMessage(R.string.clear_all_confirm)
                    .setPositiveButton(R.string.clear_all_btn, (dialog, which) -> {
                        myDB.deleteAllTasks();
                        Toast.makeText(SettingsActivity.this, "All tasks cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }
}
