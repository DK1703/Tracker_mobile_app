package com.example.my_application;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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

import java.util.Objects;

public class UnsubscribedStatusDialog extends AppCompatDialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.unsubscribed_status, null);

        ImageButton return_btn = view.findViewById(R.id.return_btn);
        return_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button buy_sub = view.findViewById(R.id.buy_sub);
        buy_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://qr.nspk.ru/AD10004F7SM1QOR38T1RNP592HT1U2MC?type=02&bank=100000000054&sum=4000&cur=RUB&crc=0B61";

                // Создайте Intent с действием ACTION_VIEW и передайте Uri ссылки
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                // Запустите активность, которая может обработать это Intent
                startActivity(intent);
            }
        });

        builder.setView(view);
        return builder.create();
    }
}
