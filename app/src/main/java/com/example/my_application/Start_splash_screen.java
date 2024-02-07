package com.example.my_application;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Start_splash_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        boolean isCheckBoxChecked = sharedPreferences.getBoolean("CHECKBOX_STATE", false);

        if (isCheckBoxChecked) {
            // Если CheckBox отмечен, переходим в MenuActivity
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            finish();  // Закрываем текущую активность
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}