package com.example.tellyes.examstationshow;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingActivity extends AppCompatActivity {
    private List<RoomInfo> roomInfoList=new ArrayList<RoomInfo>();
    private EditText IPAddress;
    private EditText RoomName;
    private TextView btnSave;
    private TextView btnBack;
    private TextView btnSaveIp;

    String BaseUrl;
    String roomname;
    private List<String> m=new ArrayList<String>();
    //private TextView view ;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        IPAddress=(EditText)findViewById(R.id.IPAddress);
        RoomName=(EditText)findViewById(R.id.RoomName);

        btnSave=(TextView)findViewById(R.id.btnSave);
        btnBack=(TextView)findViewById(R.id.btnBack);
        btnSaveIp=(TextView)findViewById(R.id.btnSaveIp);
        spinner = (Spinner) findViewById(R.id.Spinner01);

        //将可选内容与ArrayAdapter连接起来
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);
          //设置下拉列表的风格
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         //将adapter 添加到spinner中
         spinner.setAdapter(adapter);
         //添加事件Spinner事件监听
         spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
         //设置默认值
         spinner.setVisibility(View.VISIBLE);

        SharedPreferences userInfo = getSharedPreferences("user_info",0);
        //获取设置的服务器IP和房间
        BaseUrl =userInfo.getString("ipconfig", null);
        roomname=userInfo.getString("roomname", null);
        if(BaseUrl!=null)
        {
            IPAddress.setText(BaseUrl);
            GetRoomInfoUTF8();
        }
        else
        {
            IPAddress.setText("");
        }
        if(roomname!=null) {
            RoomName.setText(roomname);
        }
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IPAddress.getText().toString()=="")
                {
                    AlertMessage("IP地址必须填写。");
                    return;
                }
                if(RoomName.getText().toString()=="")
                {
                    AlertMessage("房间不能为空。");
                    return;
                }
                Pattern pattern = Pattern
                        .compile("(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2}:\\d{0,5}$)");
                Matcher matcher = pattern.matcher(IPAddress
                        .getText().toString()); // 以验证127.400.600.2为例
                if (matcher.find()) {
                   // Toast.makeText(getApplicationContext(), "设置成功",
                            //Toast.LENGTH_LONG).show();
                    SharedPreferences userInfo = getSharedPreferences(
                            "user_info", 0);

                    userInfo.edit()
                            .putString(
                                    "ipconfig",
                                    IPAddress.getText()
                                            .toString())
                            .putString("roomname", RoomName.getText()
                                    .toString())
                            .commit();

                    AlertMessage("设置信息成功。");
                    //保存成功之后返回首页
                    Intent intent = new Intent();
                    intent.setClass(SettingActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    AlertMessage("设置信息失败。");
                    //Toast.makeText(getApplicationContext(), "设置信息失败",
                           // Toast.LENGTH_LONG);
                }
            }
        });
        //保存IP地址按钮
        btnSaveIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IPAddress.getText().toString() == "") {
                    AlertMessage("IP地址必须填写。");
                    return;
                }
                Pattern pattern = Pattern
                        .compile("(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2}:\\d{0,5}$)");
                Matcher matcher = pattern.matcher(IPAddress
                        .getText().toString()); // 以验证127.400.600.2为例
                if (matcher.find()) {
                    // Toast.makeText(getApplicationContext(), "设置成功",
                    //Toast.LENGTH_LONG).show();
                    SharedPreferences userInfo = getSharedPreferences(
                            "user_info", 0);
                    userInfo.edit()
                            .putString(
                                    "ipconfig",
                                    IPAddress.getText()
                                            .toString())
                            .commit();
                    BaseUrl = IPAddress.getText().toString();
                    AlertMessage("IP地址设置信息成功。");
                    GetRoomInfoUTF8();
                }
                else {
                    AlertMessage("IP地址格式错误，设置信息失败。");
                    //Toast.makeText(getApplicationContext(), "设置信息失败",
                    // Toast.LENGTH_LONG);
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this, MainActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
//使用数组形式操作
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            TextView tv = (TextView)arg1;
            tv.setTextSize(20.0f);    //设置大小
            //只截取前面的房间号
            RoomName.setText(m.get(arg2).substring(0,m.get(arg2).indexOf("(")));
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    //获取房间信息
    private void GetRoomInfoUTF8(){
        String url="http://";
        url=url+BaseUrl+"/AppDataInterface/ExamInfoShow.aspx/GetRoomInfoUTF8";
        Ion.with(this)
                .load(url)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if (e != null) {
                            return;
                        }
                        //////
                        try {
                            int index=0;
                            Gson gson = new Gson();
                            java.lang.reflect.Type type = new TypeToken<List<RoomInfo>>() {
                            }.getType();
                            m.clear();
                            roomInfoList = gson.fromJson(result,type);
                           for(int i=0;i<roomInfoList.toArray().length;i++)
                           {
                               if(roomInfoList.get(i).RoomName.equals(RoomName.getText().toString()))
                               {
                                   index=i;
                               }
                               if(roomInfoList.get(i).State.equals("1")) {
                                   m.add(roomInfoList.get(i).RoomName+"(有考试)");
                               }
                               else {
                                   m.add(roomInfoList.get(i).RoomName+"(没有考试)");
                               }
                           }
                            adapter.notifyDataSetChanged();
                            //设置选中房间
                            if(RoomName.getText().toString()!="")
                            {
                                spinner.setSelection(index);
                            }

                        } catch (Exception eJson) {
                            AlertMessage("网络连接错误，获取房间信息失败。");
                        }

                    }
                });
    }
    private void AlertMessage(String strMsg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("提示").setMessage(strMsg)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                //按钮事件
                            }
                        })
                .show();
    }
}
