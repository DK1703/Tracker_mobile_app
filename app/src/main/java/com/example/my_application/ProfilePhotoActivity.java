package com.example.my_application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;

public class ProfilePhotoActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 2;
    private ImageView profileImageView;
    private Button selectPhotoButton;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_photo);

        profileImageView = findViewById(R.id.profileImageView);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        storageReference = FirebaseStorage.getInstance().getReference();
        

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchChoosePictureIntent();
            }
        });

        // Добавьте обработку события для кнопки "продолжить"
        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Здесь добавьте код для перехода на WelcomeActivity
                Intent welcomeIntent = new Intent(ProfilePhotoActivity.this, WelcomeActivity.class);
                startActivity(welcomeIntent);
            }
        });
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select your image from here..."), REQUEST_IMAGE_PICK);
    }

    private void uploadProfileImageToFirebaseStorage(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "Выберите изображение", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "profile_image.jpg"; // Имя файла, которое вы хотите использовать
        StorageReference imageRef = storageReference.child("profile_images/" + fileName);

        // Преобразуйте URI изображения в байты
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);

            // Загрузка изображения в Firebase Storage
            imageRef.putBytes(bytes)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ProfilePhotoActivity.this, "Изображение успешно загружено", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfilePhotoActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchChoosePictureIntent() {
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, REQUEST_IMAGE_PICK);
    }

    // Добавьте метод для обработки результата выбора фото
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Toast.makeText(this, "data not empty", Toast.LENGTH_SHORT).show();
                profileImageView.setImageURI(selectedImageUri);
                uploadProfileImageToFirebaseStorage(selectedImageUri);
                selectPhotoButton.setText("Новый текст кнопки");
            }
        }

    }
}
