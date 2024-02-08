package com.example.my_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 2;
    private TextView user_profile_name;
    private CircleImageView user_profile_photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_button);

        Button rename_profile = findViewById(R.id.rename_profile);
        Button change_photo = findViewById(R.id.change_photo);
        Button return_btn = findViewById(R.id.return_btn);
        user_profile_name = findViewById(R.id.user_profile_name);
        user_profile_photo = findViewById(R.id.user_profile_image);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        // Создайте ссылку на файл с изображением профиля в Firebase Storage
        String fileName = userId + "_profile_image.jpg";// Имя файла, которое вы использовали для сохранения изображения
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(fileName);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
             @Override
             public void onSuccess(Uri uri) {
                 Picasso.get().load(uri).placeholder(R.drawable.profile).into(user_profile_photo);
//                 saveProfileImageUriToSharedPreferences(uri.toString());
             }
         });

        FirebaseUser user_name = FirebaseAuth.getInstance().getCurrentUser();
        String userId_name = user_name.getUid();
        DatabaseReference databaseReference_name = FirebaseDatabase.getInstance().getReference("Users").child(userId_name).child("Name");
        databaseReference_name.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.getValue(String.class);
                    TextView user_profile_name = findViewById(R.id.user_profile_name);
                    user_profile_name.setText(userName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rename_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameProfileDialog();
            }
        });

        change_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to pick an image
                dispatchChoosePictureIntent();
            }
        });

        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, MenuActivity.class));
            }
        });
    }

    private String getProfileImageUriFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("PROFILE_IMAGE_URI", "");
    }

    private void showRenameProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rename_profile, null);
        builder.setView(dialogView)
                .setTitle("Rename Profile")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editTextNewName = dialogView.findViewById(R.id.editTextNewName);
                        String newName = editTextNewName.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            updateNameInFirebase(newName);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateNameInFirebase(String newName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Name");
            if (!newName.isEmpty()) {
                databaseReference.setValue(newName)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                user_profile_name.setText(newName);
                                Toast.makeText(SettingsActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                                // Передайте измененное имя обратно в MenuActivity
                                Intent intent = new Intent();
                                intent.putExtra("NEW_NAME", newName);
                                setResult(RESULT_OK, intent);
                                startActivity(new Intent(SettingsActivity.this, MenuActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingsActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(SettingsActivity.this, "Fill a name", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchChoosePictureIntent() {
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, REQUEST_IMAGE_PICK);
    }

//    private void saveProfileImageUriToSharedPreferences(String profileImageUri) {
//        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("NEW_IMAGE_URI", profileImageUri);
//        editor.apply();
//    }

    private void updateProfileImage(Uri imageUri) {
        // Сохраните URI изображения в Firebase Storage
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        // Создайте ссылку на файл с изображением профиля в Firebase Storage
        String fileName = userId+ "_profile_image.jpg";// Имя файла, которое вы использовали для сохранения изображения
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profile_images").child(fileName);
        imageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Получите URI загруженного изображения из Firebase Storage
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Сохраните URI изображения в SharedPreferences
//                        saveProfileImageUriToSharedPreferences(uri.toString());
                        Picasso.get().load(uri).placeholder(R.drawable.profile).into(user_profile_photo);
                        Intent intent = new Intent();
                        intent.putExtra("NEW_IMAGE_URI", uri.toString());
                        setResult(RESULT_OK, intent);
                        startActivity(new Intent(SettingsActivity.this, MenuActivity.class));
                        // Закройте текущую активность
                        finish();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Обработка ошибок загрузки изображения в Firebase Storage
                Toast.makeText(SettingsActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                user_profile_photo.setImageURI(selectedImageUri);
                updateProfileImage(selectedImageUri);
            }
        }
    }
}
