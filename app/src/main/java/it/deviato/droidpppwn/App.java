package it.deviato.droidpppwn;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.File;

public class App extends Application {
    protected static Context actx;
    protected static SharedPreferences settings;
    protected static SharedPreferences.Editor editset;
    protected static int selFw,selPayload;
    protected static String selIface,path,pppstr;  //OLDpath="/data/data/it.deviato.droidpppwn/lib/";
    protected static Boolean selNw,selRs,autoRun,autoShut,oldIp,isRunning=false;
    protected static final String EOL=System.getProperty("line.separator");

    @Override
    public void onCreate() {
        super.onCreate();
        actx=getApplicationContext();
        path=getApplicationInfo().nativeLibraryDir;
        if(!path.endsWith("/")) path+="/";
        //settings=getSharedPreferences("settings",Context.MODE_PRIVATE);
        settings=PreferenceManager.getDefaultSharedPreferences(this);
        editset=settings.edit();
        selFw=settings.getInt("FW",0);
        selIface=settings.getString("IFACE","eth0");
        selPayload=settings.getInt("PAYLOAD",0);
        selNw=settings.getBoolean("NW",false);
        selRs=settings.getBoolean("RS",false);
        autoRun=settings.getBoolean("ARUN",false);
        autoShut=settings.getBoolean("ASHUT",false);
        oldIp=settings.getBoolean("OLD",false);
        Log.d("Droid","aRun:"+autoRun);
    }

    protected static void buildPPPString() {
        String[] fws=actx.getResources().getStringArray(R.array.fwValues);
        String fw=fws[selFw];
        //path=getApplicationInfo().nativeLibraryDir;
        //if(!path.endsWith("/")) path+="/";
        //Log.d("Droid",path);
        String opts="";
        if(selNw) opts+=" -nw";
        if(selRs) opts+=" -rs";
        if(oldIp) opts+=" -old";
        String stage1=path+"stage1."+fw;
        String stage2=path+"stage2."+fw;
        //Linux special case to extend in future
        if(fw.equals("1100")&&selPayload==1) stage2=path+"linux.1100";
        //Prefer custom stage1.bin and/or stage2.bin from /sdcard/ if found
        File file=new File("/sdcard/stage1.bin");
        if(file.exists()) stage1="/sdcard/stage1.bin";
        file=new File("/sdcard/stage2.bin");
        if(file.exists()) stage2="/sdcard/stage2.bin";
        pppstr="/system/bin/ifconfig "+selIface+" 10.0.0.1 up 2>&1\n";
        pppstr+="export PATH=$PATH:"+path+"\nLD_LIBRARY_PATH="+path+" "; //LD_PRELOAD="+path+"libpcap.so.1 ");
        pppstr+=path+"pppwn -i "+selIface+" --fw "+fw+" --stage1 "+stage1+" --stage2 "+stage2+opts+" -a 2>&1\nexit\n";
    }

}
