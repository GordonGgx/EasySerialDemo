package com.ggx.serialportdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ggx.serialport.SerialPort;
import com.ggx.serialport.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,Runnable{

    public static final String TAG="MainActivity";

    OutputStream out;
    InputStream in;
    byte[] buffer=new byte[1024];
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send).setOnClickListener(this);
        tv= (TextView) findViewById(R.id.tv);
        //获取串口的设备名和设备路径，通过设备路径可以找到一个即可读又可写的串口作为使用
        SerialPortFinder finder=new SerialPortFinder();
        String[] devices=finder.getAllDevices();
        String[] devicesPath=finder.getAllDevicesPath();
        StringBuilder builder=new StringBuilder("设备：\n");
        for (String dev:devices){
            builder.append(dev);
            builder.append("\n");
        }
        builder.append("----------------------------");
        builder.append("设备路径：\n");
        for (String devPath:devicesPath){
            builder.append(devPath);
            builder.append("\n");
        }
        tv.setText(builder);
        //初始化串口
        try {
            SerialPort serialPort= new SerialPort(new File("/dev/ttyUSB4"),115200,0);
            out=serialPort.getOutputStream();
            in=serialPort.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //开启读监听
        new Thread(this).start();
    }
 

    @Override
    public void onClick(View view) {
        try {
            out.write(new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void run() {
        while (true){
            try {
                //获取流中可读的字节数,用来做循环监听
                int count=in.available();
                if(count<=0){
                    continue;
                }
                Log.i(TAG,"当前可获取"+count+"字节");
                //一次性读取全部的数据到字节数组中，返回所读取的真实数量
                int total=in.read(buffer,0,count);
                Log.i(TAG,"共读了"+total);
                StringBuilder builder=new StringBuilder();
                for (byte byt:buffer){
                    builder.append(byt);
                    builder.append(" ");
                }
                Log.i(TAG,"获取信息："+builder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
