package com.example.my_application;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Messenger;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AddDialog extends AppCompatDialogFragment {
    private OnCounterUpdateListener onCounterUpdateListener;

    public void setOnCounterUpdateListener(OnCounterUpdateListener listener) {
        this.onCounterUpdateListener = listener;
    }

    private TextView clockTextView;

    private final Handler handler = new Handler();
    private EditText activ_text;
    static final int NOTIFICATION_ID = 1; // Идентификатор уведомления

    private EditText descr_text;
    private int num_of_Entries_counter;

    private NumberPicker minutePicker;

    public void setCurrentCounterValue(int currentCounterValue) {
        this.num_of_Entries_counter = currentCounterValue;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder addWindow = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.add_dialog, null);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String userId = user.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("entries");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    num_of_Entries_counter = (int) (10 - snapshot.getChildrenCount());
                } else {
                    num_of_Entries_counter = 10;
                }
                TextView num_of_Entries = view.findViewById(R.id.num_of_entries);
                num_of_Entries.setText(String.valueOf(num_of_Entries_counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        minutePicker = view.findViewById(R.id.minutePicker);
    // Устанавливаем минимальное и максимальное значение для таймера (например, от 1 до 140 минут)
        minutePicker.setMinValue(1);
        minutePicker.setMaxValue(140);

        clockTextView = view.findViewById(R.id.clockTextView);
        activ_text = view.findViewById(R.id.activity);
        descr_text = view.findViewById(R.id.description);


        // Запуск обновления времени каждую секунду
        handler.postDelayed(updateTimeRunnable, 1000);

        Button saveEntryButton = view.findViewById(R.id.save_entry);
        saveEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimerAndShowNotification();
                onSaveClicked();
            }
        });

        ImageButton return_btn = view.findViewById(R.id.return_btn);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        addWindow.setView(view);

        return addWindow.create();
    }

    private void startTimerAndShowNotification() {

        String activity_text = activ_text.getText().toString();
        if (!activity_text.isEmpty()) {
            // Получение значения из NumberPicker
            int selectedMinutes = minutePicker.getValue();

            // Создание интента для запуска сервиса
            Activity activity = getActivity();
            if (activity != null) {
                // Контекст активности доступен, можно запускать сервис
                Intent serviceIntent = new Intent(activity, TimerService.class);
                serviceIntent.putExtra("selectedMinutes", selectedMinutes);
                activity.startService(serviceIntent);
                Log.d("AddDialog", "Activity context is not null");
            } else {
                // Контекст активности не доступен
                Log.e("AddDialog", "Activity context is null");
            }

            // Создание интента для запуска приложения при нажатии на уведомление
            Intent intent = new Intent(getActivity(), MenuActivity.class);
            PendingIntent pendingIntent = PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Создание уведомления
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "channel_id")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Таймер")
                    .setContentText("Время таймера: " + selectedMinutes + " минут")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true); // Уведомление закроется после нажатия на него

            // Отображение уведомления
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(123, builder.build());
            SharedPreferences prefs = getActivity().getSharedPreferences("timer_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("timer_running", true); // Здесь нужно установить true, когда таймер запускается
            editor.apply();
        } else {
            Toast.makeText(getActivity(), "Заполните поле активности", Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            // Повторное запуск каждую секунду
            handler.postDelayed(this, 1000);
        }
    };

    private void updateClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        clockTextView.setText(currentTime);
    }


    private void onSaveClicked() {
        // Получаем данные из диалогового окна, которые нужно сохранить
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        String timer = String.valueOf(minutePicker.getValue());
        String activity = activ_text.getText().toString();
        String description = descr_text.getText().toString();

        if (num_of_Entries_counter > 0  && !activity.isEmpty()) {
            UserData userData = new UserData(currentTime, timer, activity, description);

            // Получаем текущего пользователя
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Получаем уникальный идентификатор пользователя (uid)
                String userId = user.getUid();

                // Получаем ссылку на пользователя в базе данных
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                // Сохраняем данные в базе данных Firebase
                DatabaseReference entriesReference = userReference.child("entries").push();
                entriesReference.setValue(userData);

                // Печатаем идентификатор новой записи для отладки
                String entryId = entriesReference.getKey();
                Log.d("Firebase", "ID новой записи: " + entryId);
            }

            // Обновляем счетчик и внешний интерфейс
            num_of_Entries_counter--;
            updateCounterValue(num_of_Entries_counter, activity, description);
            updateRemainingEntriesCounter();

            dismiss();
        } else if (activity.isEmpty()) {
            Toast.makeText(getActivity(), "Заполните поле активности", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Купи подписку и будет неограниченное количество записей!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRemainingEntriesCounter() {
        // Используйте getDialog().findViewById, так как num_of_entries находится в макете add_dialog.xml
        TextView num_of_entries = Objects.requireNonNull(getDialog()).findViewById(R.id.num_of_entries);
        if (num_of_entries != null) {
            num_of_entries.setText(String.valueOf(num_of_Entries_counter));
        }
    }

    private void updateCounterValue(int newValue, String activityText, String descriptionText) {

        if (onCounterUpdateListener != null) {
            onCounterUpdateListener.onCounterUpdated(newValue);
        }
    }

    @Override
    public void onDestroy() {
        // Остановка обновления времени при завершении активности
        handler.removeCallbacks(updateTimeRunnable);
        super.onDestroy();
    }
}
