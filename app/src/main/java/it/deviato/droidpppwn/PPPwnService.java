package it.deviato.droidpppwn;

import static it.deviato.droidpppwn.App.*;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

import it.deviato.droidpppwn.MainActivity.AsyncExec;

public class PPPwnService extends Service {
    private static Process pbg;
    private static AsyncBg abg;
    public PPPwnService() {}

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("Droid","-ServiceOnStartCommand-");
        createNotificationChannel();
        Notification notif=createNotification("Service Started...");
        abg=new AsyncBg();
        abg.execute();
        NotificationManager ngr=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(ngr!=null) ngr.notify(1337,notif);
        //startForeground(1337,notif);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("Droid","-ServiceOnDestroy-");
        Notification notif=createNotification("Service Stopped");
        NotificationManager ngr=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(ngr!=null) ngr.notify(1337,notif);
        super.onDestroy();
    }

    protected static void startstopService() {
        Intent service=new Intent(actx,PPPwnService.class);
        if(autoRun) {
            Log.d("Droid","#StartingService");
            isRunning=true;
            MainActivity.killExploit();
            actx.startService(service);
        }
        else {
            Log.d("Droid","#StoppingService");
            actx.stopService(service);
            if(abg!=null) abg.cancel(true);
            if(pbg!=null) pbg.destroy();
            isRunning=false;
        }
    }
    protected class AsyncBg extends AsyncTask {
        @Override
        protected String doInBackground(Object[] objects) {
            String line = "";
            try {
                buildPPPString();
                pbg=Runtime.getRuntime().exec("su");
                DataOutputStream os=new DataOutputStream(pbg.getOutputStream());
                os.writeBytes(pppstr);
                os.flush();
                Log.d("Droid","-Start exploiting: "+pppstr);
                BufferedReader in=new BufferedReader(new InputStreamReader(pbg.getInputStream()));
                while ((line=in.readLine())!=null) {
                    publishProgress(line);
                }
                in.close();
                pbg.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void publishProgress(String str) {
            Log.d("Droid",str);
            if(str.equals("[+] Done!")) {
                Log.d("Droid", "-Exploit succeeded!");
                if(autoShut) MainActivity.runCmd("reboot -p", true);
                else {
                    Notification n=createNotification("Exploit Succeeded!");
                    NotificationManager ngr=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if(ngr!=null) ngr.notify(1337,n);
                    //TODO: TurnOff the service?
                }
            }
        }
    }
    private Notification createNotification(String message) {
        PendingIntent intent=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_MUTABLE);
        return new NotificationCompat.Builder(this,"DroidPPPwn")
                .setContentTitle("DroidPPPwn").setContentText(message)
                .setSmallIcon(com.google.android.material.R.drawable.notification_bg)
                .setContentIntent(intent).build();
    }
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel ntc=new NotificationChannel("DroidPPPwn","DroidPPPwn",NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(ntc);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}