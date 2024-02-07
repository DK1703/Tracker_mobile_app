package com.example.my_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class HistoryActivity extends AppCompatActivity {

    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(); // Инициализируйте адаптер
        recyclerView.setAdapter(historyAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        // Получите ссылку на базу данных Firebase и определите путь к вашей таблице "entries"
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("entries");

        // Добавьте слушателя значений для получения данных
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserData> userDataList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("Firebase", "Snapshot key: " + snapshot.getKey());
                    UserData userData = snapshot.getValue(UserData.class);
                    if (userData != null) {
                        userDataList.add(userData);
                        Log.d("Firebase", "Added userData: " + userData.toString());
                        Log.d("HistoryActivity", "userDataList size: " + userDataList.size());
                    } else {
                        Log.e("Firebase", "userData is null for key: " + snapshot.getKey());
                    }
                }
                if (!userDataList.isEmpty()) {
                    historyAdapter.setData(userDataList);
                } else {
                    Toast.makeText(getApplicationContext(), "userDataList пуст", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок при чтении из базы данных
                Log.e("Firebase", "Ошибка при чтении из базы данных: " + databaseError.getMessage());
                Toast.makeText(getApplicationContext(), "Произошла ошибка при чтении данных из базы данных", Toast.LENGTH_SHORT).show();
            }
        });

        Button return_btn = findViewById(R.id.return_btn);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HistoryActivity.this, MenuActivity.class));
            }
        });
    }
}


