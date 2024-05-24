package it.deviato.droidpppwn;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private TextView txtOut;
    private Spinner spnFW;
    private Process p;
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
                if(btnStart.isChecked()) startExploit();
                else {
                    p.destroy();
                    txtOut.setText("");
                }
            }
        });
    }

    protected void startExploit() {
        txtOut.setText("----------[Exploit-Started]----------"+EOL);
        AsyncExec ae=new AsyncExec();
        ae.execute();
    }
    private class AsyncExec extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String line="";
            String[] fws=getResources().getStringArray(R.array.fwValues);
            String fw=fws[spnFW.getSelectedItemPosition()];
            try {
                String path=getApplicationInfo().nativeLibraryDir+"/";
                Log.d("Droid","Attempting to run: "+path+"pppwnarm");
                p=Runtime.getRuntime().exec("su");
                //p=Runtime.getRuntime().exec("su -c ping www.google.it");
                DataOutputStream os=new DataOutputStream(p.getOutputStream());
                os.writeBytes("/system/bin/ifconfig eth0 10.0.0.1 up 2>&1\n");
                os.flush();
                os.writeBytes("export PATH="+path+":$PATH\nLD_LIBRARY_PATH="+path+" "); //LD_PRELOAD="+path+"libpcap.so.1 ");
                os.writeBytes("pppwnarm -i eth0 --fw "+fw+" --stage1 stage1."+fw+" --stage2 stage2."+fw+" -a 2>&1\nexit\n");
                os.flush();
                BufferedReader in=new BufferedReader(new InputStreamReader(p.getInputStream()));
                while((line=in.readLine())!=null) {
                    publishProgress(line+EOL);
                }
                in.close();
                p.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        private void publishProgress(String str) {
            txtOut.append(str);
            try {
                Thread.sleep(10);  //Fix java.lang.IndexOutOfBoundsException: offset(x) should be less than line limit(0)
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}