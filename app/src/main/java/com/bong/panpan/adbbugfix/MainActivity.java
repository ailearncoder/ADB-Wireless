package com.bong.panpan.adbbugfix;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.OutputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @InjectView(R.id.textView)
    TextView textView;
    @InjectView(R.id.button)
    Button button;
    @InjectView(R.id.editText)
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        try {
            set(editText.getText().toString());
            textView.setText("成功");
        } catch (Exception e) {
            e.printStackTrace();
            textView.setText(e.getMessage());
        }
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        try {
            set(editText.getText().toString());
            textView.setText("成功");
        } catch (Exception e) {
            e.printStackTrace();
            textView.setText(e.getMessage());
        }
    }

    protected static int set(String paramInt) throws Exception {
        Process localProcess = Runtime.getRuntime().exec("su");
        DataOutputStream localDataOutputStream = new DataOutputStream(
                (OutputStream) localProcess.getOutputStream());
        localDataOutputStream.writeBytes("setprop service.adb.tcp.port "
                + paramInt + "\n");
        localDataOutputStream.flush();
        localDataOutputStream.writeBytes("stop adbd\n");
        localDataOutputStream.flush();
        localDataOutputStream.writeBytes("start adbd\n");
        localDataOutputStream.flush();
        localDataOutputStream.writeBytes("exit\n");
        localDataOutputStream.flush();
        localProcess.waitFor();
        return localProcess.exitValue();
    }
}
