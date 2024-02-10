package com.example.my_application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "Received intent: " + intent.getAction());
        String action = intent.getAction();
        if ("STOP_TIMER_ACTION".equals(action)) {
            Intent stopTimerIntent = new Intent(context, TimerService.class);
            stopTimerIntent.setAction("STOP_TIMER_ACTION");
            context.startService(stopTimerIntent);
            Log.d("NotificationReceiver", "Sent STOP_TIMER_ACTION to TimerService");
        }
    }
}



