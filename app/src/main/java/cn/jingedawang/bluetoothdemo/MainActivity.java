package cn.jingedawang.bluetoothdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;

    private TextView txtIsConnected;
    private EditText edtReceivedMessage;
    private EditText edtSentMessage;
    private EditText edtSendMessage;
    private Button btnSend;
    private Button btnPairedDevices;

    private BluetoothAdapter mBluetoothAdapter;
    private ConnectedThread mConnectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtIsConnected = (TextView) findViewById(R.id.txtIsConnected);
        edtReceivedMessage = (EditText) findViewById(R.id.edtReceivedMessage);
        edtSentMessage = (EditText) findViewById(R.id.edtSentMessage);
        edtSendMessage = (EditText) findViewById(R.id.edtSendMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnPairedDevices = (Button) findViewById(R.id.btnPairedDevices);

        btnPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 获取蓝牙适配器
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                }

                //请求开启蓝牙
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

                //进入蓝牙设备连接界面
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), DevicesListActivity.class);
                startActivity(intent);

            }
        });

        //点击【发送】按钮后，将文本框中的文本按照ASCII码发送到已连接的蓝牙设备
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtSendMessage.getText().toString().isEmpty()) {
                    return;
                }
                String sendStr = edtSendMessage.getText().toString();
                char[] chars = sendStr.toCharArray();
                byte[] bytes = new byte[chars.length];
                for (int i=0; i < chars.length; i++) {
                    bytes[i] = (byte) chars[i];
                }
                edtSentMessage.append(sendStr);
                mConnectedThread.write(bytes);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //回到主界面后检查是否已成功连接蓝牙设备
        if (BluetoothUtils.getBluetoothSocket() == null || mConnectedThread != null) {
            txtIsConnected.setText("未连接");
            return;
        }

        txtIsConnected.setText("已连接");

        //已连接蓝牙设备，则接收数据，并显示到接收区文本框
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case ConnectedThread.MESSAGE_READ:
                        byte[] buffer = (byte[]) msg.obj;
                        int length = msg.arg1;
                        for (int i=0; i < length; i++) {
                            char c = (char) buffer[i];
                            edtReceivedMessage.getText().append(c);
                        }
                        break;
                }

            }
        };

        //启动蓝牙数据收发线程
        mConnectedThread = new ConnectedThread(BluetoothUtils.getBluetoothSocket(), handler);
        mConnectedThread.start();

    }
}
