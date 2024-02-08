package com.example.my_application;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private int savedEntriesCounter = 10, num_of_Entries_counter;
    private static final int SETTINGS_REQUEST_CODE = 1001;
    private CircleImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.menu);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        View headerView = navigationView.getHeaderView(0);
        FirebaseUser user_for_name = FirebaseAuth.getInstance().getCurrentUser();
        String userId_for_name = user_for_name.getUid();
        DatabaseReference databaseReference_for_name = FirebaseDatabase.getInstance().getReference("Users").child(userId_for_name).child("Name");
        databaseReference_for_name.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.getValue(String.class);
                    // Теперь у вас есть значение поля "Name", которое вы можете установить в TextView
                    TextView userNameTextView = headerView.findViewById(R.id.user_profile_name);
                    userNameTextView.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибки, если не удается получить данные
                Log.e("Firebase", "Failed to get user name.", databaseError.toException());
            }
        });
        profileImageView = headerView.findViewById(R.id.user_profile_image);

        loadProfileImageFromFirebaseStorage();

//        setDailyNotification();

        Button return_btn= findViewById(R.id.return_btn);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });

        ImageButton exit_btn = findViewById(R.id.exit_btn);
        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });

        Button rating_btn = findViewById(R.id.rating_btn);
        rating_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog(savedEntriesCounter);
                savedEntriesCounter--;
            }
        });

        TiredImageView tiredImageView = findViewById(R.id.tiredImageView);
        int userSelectedFatigueLevel = 0;
        tiredImageView.setFatigueLevel(userSelectedFatigueLevel);
        TextView fatigueValueTextView = findViewById(R.id.fatigueValueTextView);

        SeekBar fatigueSeekBar = findViewById(R.id.fatigueSeekBar);
        fatigueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tiredImageView.setFatigueLevel(progress);
                int invertedProgress = seekBar.getMax() - progress;
                fatigueValueTextView.setText(String.valueOf(invertedProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            String userId = user.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("entries");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        num_of_Entries_counter = (int) (10 - snapshot.getChildrenCount());
                    }
                    else {
                        num_of_Entries_counter = 10;
                    }
                    TextView count_text = findViewById(R.id.num_of_entries);
                    count_text.setText(String.valueOf(num_of_Entries_counter));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        if (getIntent().getBooleanExtra("EXIT", false)){
            finish();
        }
    }

    private void loadProfileImageFromFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        // Создайте ссылку на файл с изображением профиля в Firebase Storage
        String fileName = userId + "_profile_image.jpg";// Имя файла, которое вы использовали для сохранения изображения
        StorageReference profileImageRef = storageRef.child("profile_images/" + fileName);

        // Загрузите изображение из Firebase Storage в виде URL
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Загрузка изображения прошла успешно
                // Используйте Picasso или другую библиотеку для загрузки изображения из URI
                Picasso.get().load(uri).placeholder(R.drawable.profile).into(profileImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Обработка ошибок загрузки изображения
                Toast.makeText(MenuActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.acc_status){
            checkAccountStatus();
        } else if(id == R.id.history){
            historyLauncher.launch(new Intent(MenuActivity.this, HistoryActivity.class));
        } else if(id == R.id.logout){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        signOut();
                    }
        } else if (id == R.id.setting_icon) {
            Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void setDailyNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Установим уведомление на определенное время, например, каждый день в 14 дня
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                alarmIntent
        );
    }

    private void signOut() {
//        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//        userReference.child("session_start_time").removeValue();
//        Toast.makeText(getApplicationContext(), "Вы успешно вышли из сессии", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        // После выхода из аккаунта, переходите обратно в MainActivity
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Получите измененное имя из данных возвращенных из SettingsActivity
            String newName = data.getStringExtra("NEW_NAME");
            // Обновите имя в TextView
            TextView userNameTextView = findViewById(R.id.user_profile_name);
            userNameTextView.setText(newName);
//            String newImageUri = data.getStringExtra("NEW_IMAGE_URI");
//            if (newImageUri != null && !newImageUri.isEmpty()) {
//                // Update the ImageView in MenuActivity
//                Picasso.get().load(Uri.parse(newImageUri)).placeholder(R.drawable.profile).into(profileImageView);
//            }
        }
    }

    private void checkAccountStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        databaseReference.child("Account Status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String accountStatus = dataSnapshot.getValue(String.class);

                // Отобразите диалоговое окно в зависимости от статуса аккаунта
                showStatusDialog(accountStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок чтения из базы данных
            }
        });
    }

    private void showStatusDialog(String accountStatus) {
        // Отобразите соответствующее диалоговое окно в зависимости от статуса аккаунта
        if ("Subscribed".equals(accountStatus)) {
            showSubscribedStatusDialog();
        } else {
            showUnsubscribedStatusDialog();
        }
    }

    private void showSubscribedStatusDialog() {
        DialogFragment subscribedStatusDialog = new SubscribedStatusDialog();
        subscribedStatusDialog.show(getSupportFragmentManager(), "subscribed_status_dialog");
    }

    private void showUnsubscribedStatusDialog() {
        DialogFragment unsubscribedStatusDialog = new UnsubscribedStatusDialog();
        unsubscribedStatusDialog.show(getSupportFragmentManager(), "unsubscribed_status_dialog");
    }

    private void showExitDialog() {
        ExitDialog exitDialog = new ExitDialog();
        exitDialog.show(getSupportFragmentManager(), "exit_dialog");
    }

    private void showAddDialog(int currentCounterValue) {
        AddDialog addDialog = new AddDialog();
        addDialog.setCurrentCounterValue(currentCounterValue);
        addDialog.setOnCounterUpdateListener(new OnCounterUpdateListener() {
            @Override
            public void onCounterUpdated(int newCounterValue) {
                updateCounterValue(newCounterValue);
            }
        });
        addDialog.show(getSupportFragmentManager(), "show_dialog");
    }

    private void updateCounterValue(int newValue) {
        TextView num_of_entries = findViewById(R.id.num_of_entries);
        num_of_entries.setText(String.valueOf(newValue));
    }

    private final ActivityResultLauncher<Intent> historyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Здесь можно выполнить дополнительные действия после возвращения из HistoryActivity
                }
            }
    );
}