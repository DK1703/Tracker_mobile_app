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

import com.example.my_application.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.registerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.phone.getText().toString().isEmpty() || binding.password.getText().toString().isEmpty()
                        || binding.name.getText().toString().isEmpty() || binding.email.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                } else if (binding.password.getText().toString().length() < 8) {
                    Toast.makeText(getApplicationContext(), "Пароль должен быть более 8 символов", Toast.LENGTH_SHORT).show();
                } else{
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.email.getText().toString(), binding.password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
//                                        saveSessionStartTime(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("Name").setValue(binding.name.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("Phone").setValue(binding.phone.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("Email").setValue(binding.email.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("Password").setValue(binding.password.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("Account Status").setValue("Unsubscribed");
                                        startActivity(new Intent(RegisterActivity.this, ProfilePhotoActivity.class));
                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                    } else {
                                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                            // Пользователь уже зарегистрирован
                                            Toast.makeText(getApplicationContext(), "Этот пользователь уже зарегистрирован", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Неверный ввод данных", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                }
            }});

        CheckBox checkBoxRememberMe = findViewById(R.id.register_checkBox);
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

        ImageButton btn_register_return = findViewById(R.id.return_btn);
        btn_register_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                ActivityOptionsCompat from_register_to_main = ActivityOptionsCompat.makeSceneTransitionAnimation(RegisterActivity.this, btn_register_return,
                        Objects.requireNonNull(ViewCompat.getTransitionName(btn_register_return)));
                startActivity(intent, from_register_to_main.toBundle());
            }
        });
    }

//    private void saveSessionStartTime(String userId) {
//        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//
//        // Проверяем, было ли уже установлено время начала сессии
//        userReference.child("session_start_time").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    userReference.child("session_start_time").setValue(ServerValue.TIMESTAMP);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Обработка ошибок
//            }
//        });
//    }
}