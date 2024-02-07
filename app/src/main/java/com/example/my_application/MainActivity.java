package com.example.my_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn_sign_in = findViewById(R.id.sign_in);
        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                ActivityOptionsCompat from_main_to_sign_in = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, btn_sign_in,
                        Objects.requireNonNull(ViewCompat.getTransitionName(btn_sign_in)));
                startActivity(intent, from_main_to_sign_in.toBundle());
            }
        });


        Button btn_register = findViewById(R.id.register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                ActivityOptionsCompat from_main_to_register = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, btn_register,
                        Objects.requireNonNull(ViewCompat.getTransitionName(btn_register)));
                startActivity(intent, from_main_to_register.toBundle());
            }
        });

        Button language_btn = findViewById(R.id.language);
        language_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });


//        Button btn_cont = findViewById(R.id.btn_cont);
//        btn_cont.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                if (user != null) {
//                    checkSessionTimeAndNavigateToMenu(user.getUid());
//                }
//            }
//        });

        if (getIntent().getBooleanExtra("EXIT", false)){
            finish();
        }
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.language_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Получаем ссылки на элементы в диалоге
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        Button btnClose = dialogView.findViewById(R.id.btnClose);
        Button btnApply = dialogView.findViewById(R.id.btnApply);

        // Добавляем опции языка в RadioGroup
        List<LanguageOption> supportedLanguages = new ArrayList<>();
        supportedLanguages.add(new LanguageOption("English", "en"));
        supportedLanguages.add(new LanguageOption("Русский", "ru"));
        addLanguageOptions(radioGroup, supportedLanguages);

        // Обработчик нажатия на кнопку закрытия
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Обработчик нажатия на кнопку применения
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    RadioButton selectedRadioButton = dialogView.findViewById(selectedId);
                    String selectedLanguageCode = selectedRadioButton.getTag().toString();
                    setLocale(selectedLanguageCode);
                    dialog.dismiss();
                } else {
                    // Пользователь не выбрал язык
                    Toast.makeText(MainActivity.this, "Please select a language", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addLanguageOptions(RadioGroup radioGroup, List<LanguageOption> languageOptions) {
        for (LanguageOption option : languageOptions) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option.getLanguageName());
            radioButton.setTag(option.getLanguageCode());
            radioGroup.addView(radioButton);
        }
    }


    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Перезагрузка активности для применения языка
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

//    private void checkSessionTimeAndNavigateToMenu(String userId) {
//        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//        userReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Long sessionStartTime = dataSnapshot.child("session_start_time").getValue(Long.class);
//                if (sessionStartTime != null) {
//                    long currentTime = System.currentTimeMillis();
//                    long elapsedTime = currentTime - sessionStartTime;
//                    long sessionTimeout = TimeUnit.MINUTES.toMillis(3); // Например, 3 минуты
//
//                    if (elapsedTime < sessionTimeout) {
//                        // Перейти в меню
//                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
//                        startActivity(intent);
//                        finish();
//                    } else {
//                        // Время сессии истекло, отлогиниваем пользователя
//                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//                        userReference.child("session_start_time").removeValue();
//                        FirebaseAuth.getInstance().signOut();
//                        Toast.makeText(getApplicationContext(), R.string.session, Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(getApplicationContext(), "Зарегайся пж.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Обработка ошибок
//            }
//        });
//    }
}