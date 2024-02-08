package com.example.my_application;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

public class AddDialog extends AppCompatDialogFragment {
    private OnCounterUpdateListener onCounterUpdateListener;
    public void setOnCounterUpdateListener(OnCounterUpdateListener listener) {
        this.onCounterUpdateListener = listener;
    }
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextView clockTextView;
    private final Handler handler = new Handler();
    private EditText activ_text;
    private EditText descr_text;
    private int num_of_Entries_counter;

    public void setCurrentCounterValue(int currentCounterValue) {
        this.num_of_Entries_counter = currentCounterValue;
    }
    private Button voiceButtonForDescription;

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
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

// Установите слушатель для обработки результатов распознавания речи
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Вызывается, когда приложение готово к началу записи речи
            }

            @Override
            public void onBeginningOfSpeech() {
                // Вызывается в начале записи речи
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Вызывается, когда уровень аудиосигнала изменился
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Вызывается при получении звукового буфера
            }

            @Override
            public void onEndOfSpeech() {
                // Вызывается в конце записи речи
            }

            @Override
            public void onError(int error) {
                // Вызывается при возникновении ошибки распознавания речи
            }

            @Override
            public void onResults(Bundle results) {
                // Вызывается при получении результатов распознавания речи
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    // Обновите поле описания с распознанным текстом
                    String recognizedText = matches.get(0);
                    descr_text.setText(recognizedText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Вызывается при получении частичных результатов распознавания речи
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Вызывается при получении события распознавания речи
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

        voiceButtonForDescription = view.findViewById(R.id.btn_voice_for_description);
        voiceButtonForDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.startListening(speechRecognizerIntent);
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
