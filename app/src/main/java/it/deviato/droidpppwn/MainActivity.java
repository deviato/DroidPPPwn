package it.deviato.droidpppwn;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout main;
    private TextView txtOut;
    private Button btnIface;
    private Spinner spnFW;
    private CheckBox chkNw,chkRs,chkLinux;
    private ListView lvIface;
    private Process p;
    private AsyncExec ae;
    private boolean isRoot;
    private String arch;
    private String path; //="/data/data/it.deviato.droidpppwn/lib/";
    protected SharedPreferences settings;
    protected SharedPreferences.Editor editset;
    protected int selFw,selPayload;
    protected String selIface;
    protected Boolean selNw,selRs;
    private static final String EOL=System.getProperty("line.separator");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),(v,insets) -> {
            Insets systemBars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left,systemBars.top,systemBars.right,systemBars.bottom);
            return insets;
        });
        isRoot=!runCmd("which su",false).isEmpty();
        arch=runCmd("getprop ro.product.cpu.abi",false).trim();
        path=getApplicationInfo().nativeLibraryDir;
        if(!path.endsWith("/")) path+="/";
        final TextView txtArch=findViewById(R.id.txtArch);
        txtArch.setText("["+arch+"]");
        //Log.d("Droid","CPUABI:"+Build.CPU_ABI);
        //Log.d("Droid","PROPABI:"+arch);
        //Log.d("Droid", "OSARCH:"+System.getProperty("os.arch"));
        settings=this.getPreferences(Context.MODE_PRIVATE);
        editset=settings.edit();
        selFw=settings.getInt("FW",0);
        selIface=settings.getString("IFACE","eth0");
        selPayload=settings.getInt("PAYLOAD",0);
        selNw=settings.getBoolean("NW",false);
        selRs=settings.getBoolean("RS",false);
        main=findViewById(R.id.main);
        txtOut=findViewById(R.id.txtOutput);
        txtOut.setMovementMethod(new ScrollingMovementMethod());
        btnIface=findViewById(R.id.btnInterface);
        btnIface.setText(selIface);
        chkNw=findViewById(R.id.chkNw);
        chkNw.setChecked(selNw);
        chkRs=findViewById(R.id.chkRs);
        chkRs.setChecked(selRs);
        chkLinux=findViewById(R.id.chkLinux);
        chkLinux.setChecked(selPayload==1);
        spnFW=findViewById(R.id.spnFirmware);
        ArrayAdapter<CharSequence> adpFW=ArrayAdapter.createFromResource(this,R.array.fwItems,android.R.layout.simple_list_item_1);
        adpFW.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spnFW.setAdapter(adpFW);
        spnFW.setSelection(selFw);
        lvIface=findViewById(R.id.lvIface);
        lvIface.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lvIface.setVisibility(View.GONE);
                selIface=((TextView)view).getText().toString();
                btnIface.setText(selIface);
                editset.putString("IFACE",selIface);
                editset.commit();
            }
        });
        btnIface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSuMsg()) {
                    btnIface.setEnabled(false);
                    String ifnames=runCmd("/system/bin/ls /sys/class/net/", true);
                    String[] lines=ifnames.split(EOL);
                    List<String> ifaces=new ArrayList<String>();
                    for (String l : lines) {
                        if (!l.equals("lo") && !l.startsWith("dummy") && !l.startsWith("bond") && !l.startsWith("ip") && !l.startsWith("seth")
                                /*&& !l.startsWith("wlan")*/
                                && !l.startsWith("wifi") && !l.startsWith("p2p") && !l.startsWith("sit") && !l.contains("rmnet"))
                            ifaces.add(l);
                    }
                    if (ifaces.size()==0) {
                        Toast t=Toast.makeText(getApplicationContext(), "Sorry, no ethernet interface found.", Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    } else {
                        ArrayAdapter adpIface = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, ifaces);
                        lvIface.setAdapter(adpIface);
                        lvIface.setVisibility(View.VISIBLE);
                    }
                    btnIface.setEnabled(true);
                }
            }
        });
        spnFW.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,View view,int pos,long id) {
                editset.putInt("FW",pos);
                editset.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        chkLinux.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selPayload=isChecked?1:0;
                editset.putInt("PAYLOAD",selPayload);
                editset.commit();
            }
        });
        chkNw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selNw=isChecked;
                editset.putBoolean("NW",selNw);
                editset.commit();
            }
        });
        chkRs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selRs=isChecked;
                editset.putBoolean("RS",selRs);
                editset.commit();
            }
        });

        final Switch btnStart=findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnStart.isChecked()) {
                    if(checkSuMsg()) {
                        txtOut.setText("----------[Exploit-Started]----------"+EOL);
                        ae=new AsyncExec();
                        ae.execute();
                    }
                    else btnStart.setChecked(false);
                }
                else {
                    ae.cancel(true);
                    p.destroy();
                    txtOut.setText("-----[DroidPPPwn 1.3 by deviato]-----"+EOL);
                    main.setBackgroundColor(Color.WHITE);
                }
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        if(checkSuMsg()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkInstall();
                }
            }, 500);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(p!=null)  p.destroy();
    }

    private boolean checkSuMsg() {
        if(!isRoot) {
            Toast t=Toast.makeText(getApplicationContext(),"Sorry, you don't have root :(\nGet it with Magisk.",Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER,0,0);
            t.show();
        }
        return isRoot;
    };
    private void checkInstall() {
        //Check if already installed
        //File f=new File("/data/data/it.deviato.droidpppwn/lib/pppwn");
        File f=new File(path+"pppwn");
        if(!f.exists()) {
            //Check if system have unzip or busybox
            String unzip=runCmd("which unzip",false).trim();
            if(unzip.isEmpty()||unzip.contains("not found")) {
                unzip=runCmd("busybox", true).trim();
                if(unzip.isEmpty()||unzip.contains("not found")) {
                    Log.d("Droid","Error: no unzip found");
                    txtOut.append("Unzip not found. Please install BusyBox first!"+EOL);
                }
                else unzip="busybox unzip";
            }
            Log.d("Droid","UnzipTool: "+unzip);
            if(!unzip.isEmpty()) {
                String fakelib="libpppwn.so";
                //If we are on KitKat use shared build, otherwise static one
                if(arch.startsWith("armeabi-v7")&&Build.VERSION.SDK_INT<21) fakelib="libpppwnkk.so";
                /*if(arch.startsWith("armeabi-v7")) {
                    if(Build.VERSION.SDK_INT<21) fakelib="libarm7kk.so";
                    else fakelib="libarm7.so";
                }
                else if(arch.startsWith("arm64")) fakelib="libarm64.so";
                else fakelib="libx86.so";*/
                Log.d("Droid","CMD: "+unzip+" -o "+path+fakelib+" -d "+path);
                String res=runCmd(unzip+" -o "+path+fakelib+" -d "+path+"\nchmod 755 "+path+"pppwn",true);
                Log.d("Droid",res);
                if(res.contains("couldn't")) {
                    txtOut.append("Binary installation failed!"+EOL);
                }
                else if(res.contains("inflating")) {
                    txtOut.append("Binary "+fakelib+" for "+arch+" successfully installed!"+EOL);
                }
            }
        }
    }
    /*public void copyRes(String res,String dst) {
        InputStream in=getResources().openRawResource(getResources().getIdentifier(res,"raw",getPackageName()));
        try {
            //InputStream in=getAssets().open(res,"UTF-8");
            OutputStream out=new FileOutputStream("/data/local/tmp/"+dst);
            try {
                // Transfer bytes from in to out
                byte[] buf=new byte[1024];
                int len;
                while ((len=in.read(buf))>0) {
                    out.write(buf,0,len);
                }
            } finally {
                out.close();
                in.close();
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }*/
    private String runCmd(String cmd,boolean su) {
        String line;
        StringBuilder out=new StringBuilder();
        try {
            //p=Runtime.getRuntime().exec(new String[]{"sh","-c",cmd,opt});
            if(su) p=Runtime.getRuntime().exec("su");
            else p=Runtime.getRuntime().exec("sh");
            DataOutputStream os=new DataOutputStream(p.getOutputStream());
            BufferedReader in=new BufferedReader(new InputStreamReader(p.getInputStream()));
            os.writeBytes(cmd+" 2>&1\nexit\n");
            os.flush();
            while((line=in.readLine())!=null) {
                out.append(line+EOL);
            }
            in.close();
            p.waitFor();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }
    private class AsyncExec extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String line="";
            String[] fws=getResources().getStringArray(R.array.fwValues);
            String fw=fws[spnFW.getSelectedItemPosition()];
            //path=getApplicationInfo().nativeLibraryDir;
            //if(!path.endsWith("/")) path+="/";
            //Log.d("Droid",path);
            String opts="";
            if(selNw) opts+=" -nw";
            if(selRs) opts+=" -rs";
            String stage1=path+"stage1."+fw;
            String stage2=path+"stage2."+fw;
            //Linux special case to extend in future
            if(fw.equals("1100")&&selPayload==1) stage2=path+"linux.1100";
            //Prefer custom stage1.bin and/or stage2.bin from /sdcard/ if found
            File file=new File("/sdcard/stage1.bin");
            if(file.exists()) stage1="/sdcard/stage1.bin";
            file=new File("/sdcard/stage2.bin");
            if(file.exists()) stage2="/sdcard/stage2.bin";

            try {
                //Log.d("Droid","Attempting to run: "+path+"pppwn");
                p=Runtime.getRuntime().exec("su");
                //p=Runtime.getRuntime().exec("su -c ping www.google.it");
                DataOutputStream os=new DataOutputStream(p.getOutputStream());
                os.writeBytes("/system/bin/ifconfig eth0 10.0.0.1 up 2>&1\n");
                os.flush();
                //Leaving this line for compatibility with A4.4 shared build, and user custom builds
                os.writeBytes("export PATH=$PATH:"+path+"\nLD_LIBRARY_PATH="+path+" "); //LD_PRELOAD="+path+"libpcap.so.1 ");
                os.writeBytes(path+"pppwn -i "+selIface+" --fw "+fw+" --stage1 "+stage1+" --stage2 "+stage2+opts+" -a 2>&1\nexit\n");
                os.flush();
                BufferedReader in=new BufferedReader(new InputStreamReader(p.getInputStream()));
                while((line=in.readLine())!=null) {
                    publishProgress(line);
                }
                in.close();
                p.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private void publishProgress(String str) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(txtOut.getText().length()>65535) txtOut.setText("");
                    txtOut.append(str+EOL);
                    //Log.d("Droid",str);
                    if(str.equals("[+] Done!")) main.setBackgroundColor(Color.parseColor("#55ff55"));
                }
            });
        }
    }
}