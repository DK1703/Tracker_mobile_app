package com.example.my_application;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class ExitDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder exit_window = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.exit_dialog, null);

        exit_window.setView(view);

        Button yes_btn = view.findViewById(R.id.yes);
        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fromMenutoExit = new Intent(getActivity(), MainActivity.class);
                fromMenutoExit.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                fromMenutoExit.putExtra("EXIT", true);
                startActivity(fromMenutoExit);
            }
        });


        Button no_btn = view.findViewById(R.id.no);
        no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExitDialog.this.dismiss();
            }
        });
        return exit_window.create();
    }
}
