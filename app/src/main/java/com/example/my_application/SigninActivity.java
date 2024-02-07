package com.example.my_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.my_application.databinding.ActivitySigninBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class SigninActivity extends AppCompatActivity {

    private ActivitySigninBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signinBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.email.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                }else {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.email.getText().toString(), binding.password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
//                                        saveSessionStartTime(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                                        startActivity(new Intent(SigninActivity.this, MenuActivity.class));
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Пользователь не зарегистрирован", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        CheckBox checkBoxRememberMe = findViewById(R.id.signin_checkBox);
        // Восстанавливаем предыдущее состояние CheckBox из SharedPreferences

        // Добавляем обработчик изменения состояния CheckBox
        checkBoxRememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Сохраняем текущее состояние CheckBox в SharedPreferences
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("CHECKBOX_STATE", isChecked);
                editor.apply();
            }
        });

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        boolean isCheckBoxChecked = sharedPreferences.getBoolean("CHECKBOX_STATE", false);
        checkBoxRememberMe.setChecked(isCheckBoxChecked);

        ImageButton btn_return = findViewById(R.id.return_btn);
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                ActivityOptionsCompat from_sign_in_to_main = ActivityOptionsCompat.makeSceneTransitionAnimation(SigninActivity.this, btn_return,
                        Objects.requireNonNull(ViewCompat.getTransitionName(btn_return)));
                startActivity(intent, from_sign_in_to_main.toBundle());
            }
        });
    }

//    Сохраняем время сессии
//    private void saveSessionStartTime(String userId) {
//        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//
//        // Устанавливаем новое время начала сессии
//        userReference.child("session_start_time").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                userReference.child("session_start_time").setValue(ServerValue.TIMESTAMP);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Обработка ошибок
//            }
//        });
//    }

}