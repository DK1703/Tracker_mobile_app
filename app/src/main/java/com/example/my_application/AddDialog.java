package com.example.my_application;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class AddDialog extends AppCompatDialogFragment {

    private OnCounterUpdateListener onCounterUpdateListener;

    public void setOnCounterUpdateListener(OnCounterUpdateListener listener) {
        this.onCounterUpdateListener = listener;
    }

    private TextView clockTextView;
    private final Handler handler = new Handler();

    private EditText activ_text;
    private EditText descr_text;

    private int num_of_Entries_counter;

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
                }
                else {
                    num_of_Entries_counter = 10;
                }
                TextView num_of_Entries = view.findViewById(R.id.num_of_entries);
                num_of_Entries.setText(String.valueOf(num_of_Entries_counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        clockTextView = view.findViewById(R.id.clockTextView);
        activ_text = view.findViewById(R.id.activity);
        descr_text = view.findViewById(R.id.description);
//        TextView num_of_Entries = view.findViewById(R.id.num_of_entries);
//        num_of_Entries.setText(String.valueOf(num_of_Entries_counter));


        // Запуск обновления времени каждую секунду
        handler.postDelayed(updateTimeRunnable, 1000);

        Button saveEntryButton = view.findViewById(R.id.save_entry);
        saveEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
                dismiss(); // Закрываем диалог после сохранения
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
        String activity = activ_text.getText().toString();
        String description = descr_text.getText().toString();

        if (num_of_Entries_counter > 0) {
            UserData userData = new UserData(currentTime, activity, description);

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


