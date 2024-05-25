package it.deviato.droidpppwn;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout main;
    private TextView txtOut;
    private Spinner spnFW;
    private Process p;
    private AsyncExec ae;
    protected SharedPreferences settings;
    protected SharedPreferences.Editor editset;
    protected int selFw;
    private static final String EOL=System.getProperty("line.separator");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        settings=this.getPreferences(Context.MODE_PRIVATE);
        editset=settings.edit();
        selFw=settings.getInt("FW",0);
        main=findViewById(R.id.main);
        txtOut=findViewById(R.id.txtOutput);
        txtOut.setMovementMethod(new ScrollingMovementMethod());
        spnFW=findViewById(R.id.spnFirmware);
        ArrayAdapter<CharSequence> adpFW=ArrayAdapter.createFromResource(this,R.array.fwItems,android.R.layout.simple_list_item_1);
        adpFW.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFW.setAdapter(adpFW);
        spnFW.setSelection(selFw);
        spnFW.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView,View view,int i,long l) {
                editset.putInt("FW",i);
                editset.commit();
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        final Switch btnStart=findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnStart.isChecked()) {
                    txtOut.setText("----------[Exploit-Started]----------"+EOL);
                    ae=new AsyncExec();
                    ae.execute();               }
                else {
                    ae.cancel(true);
                    p.destroy();
                    txtOut.setText("");
                    main.setBackgroundColor(Color.WHITE);
                }
            }
        });
    }

    private class AsyncExec extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String line="";
            String[] fws=getResources().getStringArray(R.array.fwValues);
            String fw=fws[spnFW.getSelectedItemPosition()];
            try {
                String path=getApplicationInfo().nativeLibraryDir+"/";
                //Log.d("Droid","Attempting to run: "+path+"pppwn");
                p=Runtime.getRuntime().exec("su");
                //p=Runtime.getRuntime().exec("su -c ping www.google.it");
                DataOutputStream os=new DataOutputStream(p.getOutputStream());
                os.writeBytes("/system/bin/ifconfig eth0 10.0.0.1 up 2>&1\n");
                os.flush();
                os.writeBytes("export PATH=$PATH:"+path+"\nLD_LIBRARY_PATH=$LD_LIBRARY_PATH:"+path+" "); //LD_PRELOAD="+path+"libpcap.so.1 ");
                os.writeBytes("pppwn -i eth0 --fw "+fw+" --stage1 "+path+"stage1."+fw+" --stage2 "+path+"stage2."+fw+" -a 2>&1\nexit\n");
                os.flush();
                BufferedReader in=new BufferedReader(new InputStreamReader(p.getInputStream()));
                while((line=in.readLine())!=null) {
                    publishProgress(line);
                }
                in.close();
                p.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        private void publishProgress(String str) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtOut.append(str+EOL);
                    Log.d("Droid",str);
                    if(str.equals("[+] Done!")) main.setBackgroundColor(Color.parseColor("#55ff55"));
                }
            });
        }
    }
}