package it.deviato.droidpppwn;

import static it.deviato.droidpppwn.App.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class BootAuto extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Droid", "-BootCompleted-");
        Intent service;
        if(autoRun&&!isRunning) {
            Log.d("Droid", "#AutoStart-Service");
            isRunning=true;
            service=new Intent(context,PPPwnService.class);
            //ContextCompat.startForegroundService(context,service);
            context.startService(service);
        }
    }
}