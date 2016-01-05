package com.example.tellyes.examstationshow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    ExamUserInfo examUserInfo;
    ExamStationInfo examStationInfo;

    private TextView EsName;
    private TextView ExamName;
    //当前考生
    private TextView UserName;
    //下一站
    private TextView NextStation;
    //当前时间
    private TextView CurrentTime;
    //考试时间
    private TextView ExamTime;
    //考试状态
    private TextView ExamState;
    //剩余时间
    private TextView LeaveTime;
    //考试题目
    private TextView ExamTitle;
    //考试内容
    private TextView ExamContent;
    //下一位考生
    private TextView NextStuExamNumber;
    //文件保存路径
   // private String capturePath = null;
    String imagePath;
    //上传后的图片背景
    private ImageView imgView1;
    //当前考生采集的图片
    private ImageView imgView2;
    //三张图片控制按钮
    private ImageView btnUpload;
    private ImageView btnCamera;
    private ImageView btnClear;
    //设置按钮
    private TextView btnSet;
    //计时器
    private Chronometer chronometer;
    //用于记时，如果考试没有考试需要定期判断考试是否开始
    private Chronometer chronometer2;
    //记时五秒执行取数据
    int preS=10;
    //为了校对系统时间,暂定为2分钟，执行一次
    int CheckS=120;
    //设置当前时间
    Date SysCurrentTime=new Date();
    long IntervalDate;
    //boolean StartFlag=false;
    //是否重新启动计时器
   // boolean restratTime=false;
    GregorianCalendar cal = new GregorianCalendar();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private int startTime = 0;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE =123;
    private static final int CROP_IMAGE_REQUEST_CODE =125;
    //String tempStr=null;

    String BaseUrl;
    String roomname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EsName=(TextView)findViewById(R.id.EsName);
        ExamName=(TextView)findViewById(R.id.ExamName);
        //学生信息
        UserName=(TextView)findViewById(R.id.UserName);
        NextStation=(TextView)findViewById(R.id.NextStation);
        CurrentTime=(TextView)findViewById(R.id.CurrentTime);
        ExamTime=(TextView)findViewById(R.id.ExamTime);
        ExamState=(TextView)findViewById(R.id.ExamState);
        LeaveTime=(TextView)findViewById(R.id.LeaveTime);
        ExamTitle=(TextView)findViewById(R.id.ExamTitle);
        ExamContent=(TextView)findViewById(R.id.ExamContent);
        NextStuExamNumber=(TextView)findViewById(R.id.NextStuExamNumber);
        //显示的两张图片
        imgView1=(ImageView)findViewById(R.id.imgView1);
        imgView2=(ImageView)findViewById(R.id.imgView2);
        //三个图片控制按钮
        btnUpload=(ImageView)findViewById(R.id.btnUpload);
        btnCamera=(ImageView)findViewById(R.id.btnCamera);
        btnClear=(ImageView)findViewById(R.id.btnClear);
        //获取设置的服务器IP和房间
        SharedPreferences userInfo = getSharedPreferences("user_info",0);
        BaseUrl =userInfo.getString("ipconfig", null);
        roomname=userInfo.getString("roomname", null);
        //设置按钮
        btnSet=(TextView)findViewById(R.id.btnSet);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开设置界面
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        //上传图片按钮
        btnUpload=(ImageView)findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture();
            }
        });
        //计时器
        chronometer=(Chronometer)findViewById(R.id.chronometer);
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                //当前时间计算
                cal.add(Calendar.SECOND, 1);
                String hour = cal.get(Calendar.HOUR_OF_DAY) + "";
                if (hour.length() == 1) {
                    hour = "0" + hour;
                }
                String minute = cal.get(Calendar.MINUTE) + "";
                if (minute.length() == 1) {
                    minute = "0" + minute;
                }
                String second = cal.get(Calendar.SECOND) + "";
                if (second.length() == 1) {
                    second = "0" + second;
                }
                CurrentTime.setText(hour + "h " + minute + "m " + second + "s");
                CheckS=CheckS-1;
                //剩余时间计算,每次减一秒
                if (IntervalDate >= 1000) {
                    IntervalDate = IntervalDate - 1000;
                    //计算分和秒
                    long s = IntervalDate / 1000;
                    String m = String.valueOf(s / 60).length() == 1 ? "0" + String.valueOf(s / 60) : String.valueOf(s / 60);
                    String s1 = String.valueOf(s % 60).length() == 1 ? "0" + String.valueOf(s % 60) : String.valueOf(s % 60);
                    LeaveTime.setText(m + "m " + s1 + "s");
                    //如果校对时间已到2分钟，就调用重新加载数据
                    if (CheckS<0) {
                        CheckS = 120;
                        GetUserInfo();
                    }
                } else {
                    //StartFlag=false;
                    GetUserInfo();
                    GetStationInfoUTF8();
                }

                // 如果开始计时到现在超过了startime秒
                //if (SystemClock.elapsedRealtime()
                //  - chronometer.getBase() > startTime * 1000) {
                // chronometer.stop();

                //}
            }
        });
        chronometer2=(Chronometer)findViewById(R.id.chronometer2);
        chronometer2.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                //如果考试还没有开始，则五秒钟执行一次取数据
                preS = preS - 1;
                if (preS < 0) {
                    preS = 10;
                    GetUserInfo();
                    GetStationInfoUTF8();
                }
            }
        });
            //拍照按钮
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startCamera();
                Intent bintent = new Intent(MainActivity.this, PictureActivity.class);
               // String bsay = "Hello, this is B speaking";
                //bintent.putExtra("listenB", bsay);
                startActivityForResult(bintent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE); // 参数(Intent intent, Int requestCode) 的 requestCode 对应下面回收Bundle时识别用的


            }
        });
        //清空图片
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgView2.setImageResource(R.drawable.studentimg2);
                imagePath=null;
            }
        });
        //如果服务器IP和房间设置成功才调用加载数据的方法
        if(BaseUrl!=null && roomname!=null)
        {
            //getSystemState();
            //页面加载时
            GetUserInfo();
            GetStationInfoUTF8();
        }
        else {
            Toast.makeText(getApplicationContext(), "请先设置服务器IP和房间",
                    Toast.LENGTH_LONG).show();
        }
    }
    private void GetStationInfoUTF8(){
        String url="http://";
        url=url+BaseUrl+"/AppDataInterface/ExamInfoShow.aspx/GetStationInfoUTF8";
        Ion.with(this)
                .load(url)
                .setBodyParameter("RoomName", roomname)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            return;
                        }
                        //////
                        try {
                            Gson gson = new Gson();
                            java.lang.reflect.Type type = new TypeToken<ExamStationInfo>() {
                            }.getType();
                            examStationInfo = gson.fromJson(result, type);
                            //此时有数据
                            if (examStationInfo.result.equals("1")) {
                                EsName.setText(URLDecoder.decode(examStationInfo.EsName, "UTF-8"));
                                ExamName.setText(URLDecoder.decode(examStationInfo.ExamName, "UTF-8"));
                                ExamTitle.setText(URLDecoder.decode(examStationInfo.ExamName, "UTF-8"));
                              ExamContent.setText(URLDecoder.decode(examStationInfo.Content, "UTF-8"));
                            } else {
                                chronometer.stop();
                                chronometer2.setBase(SystemClock.elapsedRealtime());
                                // 开始记时
                                chronometer2.start();
                                EsName.setText("");
                                ExamName.setText("");
                                ExamTitle.setText("");
                                 ExamContent.setText("");

                            }

                        } catch (Exception eJson) {
                            chronometer.stop();
                            chronometer2.setBase(SystemClock.elapsedRealtime());
                            // 开始记时
                            chronometer2.start();
                            System.out.println(eJson);
                        }

                    }
                });
    }
    //获取详细信息
    private void GetUserInfo(){
        String url="http://";
        url=url+BaseUrl+"/AppDataInterface/ExamInfoShow.aspx/GetUserInfo";

     Ion.with(this)
                .load(url)
                .setBodyParameter("RoomName", roomname)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            return;
                        }
                        //////
                        try {
                            Gson gson = new Gson();
                            java.lang.reflect.Type type = new TypeToken<ExamUserInfo>() {
                            }.getType();
                            examUserInfo = gson.fromJson(result, type);
                            //此时有数据
                            if (examUserInfo.result.equals("1")) {

                                UserName.setText(URLDecoder.decode(examUserInfo.StuExamNumber, "UTF-8"));
                                NextStation.setText(URLDecoder.decode(examUserInfo.NextESName, "UTF-8"));
                                CurrentTime.setText(examUserInfo.strSystemTime);
                                ExamTime.setText(examUserInfo.StuStartTime.substring(0, examUserInfo.StuStartTime.lastIndexOf(":")) + "--" + examUserInfo.StuEndTime.substring(0, examUserInfo.StuEndTime.lastIndexOf(":")));
                                ExamState.setText(URLDecoder.decode(examUserInfo.StuState, "UTF-8"));
                                /*if (ExamState.getText().equals("考试中")) {
                                    ExamState.setTextColor(Color.GREEN);
                                }else if (ExamState.getText().equals("等待中")) {
                                    ExamState.setTextColor(Color.GREEN);
                                }else {
                                    ExamState.setTextColor(Color.BLACK);
                                }*/
                                NextStuExamNumber.setText(URLDecoder.decode(examUserInfo.NextStuExamNumber, "UTF-8"));

                                SysCurrentTime = sdf.parse(examUserInfo.strSystemTime);
                                cal.setTime(SysCurrentTime);
                                //计算当前时间所相差的秒数
                                String YMD = cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE);
                                //如果为考试中，计算剩余时间
                                if (examUserInfo.StuState.equals("考试中")) {
                                    Calendar rightNow = Calendar.getInstance();
                                    rightNow.setTime(sdf.parse(YMD + " " + examUserInfo.StuStartTime));
                                    rightNow.add(Calendar.MINUTE, Integer.parseInt(examUserInfo.strStationExamTime));
                                    IntervalDate = rightNow.getTime().getTime() - SysCurrentTime.getTime();
                                    //如果小于0，说明状态不准，都已经超过考试中
                                    if (IntervalDate < 0) {
                                        IntervalDate = sdf.parse(YMD + " " + examUserInfo.StuEndTime).getTime() - SysCurrentTime.getTime();
                                    }
                                    //IntervalDate=+(Integer.parseInt(examUserInfo.strStationExamTime)*1000)-SysCurrentTime.getTime()
                                    //IntervalDate=sdf.parse(cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DATE)+" "+examUserInfo.StuEndTime).getTime()-SysCurrentTime.getTime();
                                } else if (examUserInfo.StuState.equals("等待中")) {
                                    //等待中为开始时间－当前时间为时间差
                                    Calendar rightNow = Calendar.getInstance();
                                    rightNow.setTime(sdf.parse(YMD + " " + examUserInfo.StuStartTime));
                                    IntervalDate = rightNow.getTime().getTime() - SysCurrentTime.getTime();
                                    if (IntervalDate < 0) {
                                        IntervalDate = 0;
                                    }
                                } else {
                                    IntervalDate = sdf.parse(YMD + " " + examUserInfo.StuEndTime).getTime() - SysCurrentTime.getTime();
                                }
                                //IntervalDate=sdf.parse(cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DATE)+" "+examUserInfo.StuEndTime).getTime()-SysCurrentTime.getTime();
                                //填充个人图片
                                getCurrentUserPhoto();
                                //
                                // if(StartFlag==false)
                                // {
                                chronometer.setBase(SystemClock.elapsedRealtime());
                                // 开始记时
                                chronometer.start();
                                //如果正式考试开始，则关闭５秒运行一次的程序
                                chronometer2.stop();
                                ;
                                //StartFlag=true;
                                // }
                            } else {
                                chronometer.stop();
                                chronometer2.setBase(SystemClock.elapsedRealtime());
                                // 开始记时
                                chronometer2.start();

                                UserName.setText("");
                                NextStation.setText("本考站考试结束");
                                CurrentTime.setText("");
                                ExamTime.setText("");//examUserInfo.StuStartTime + "--" + examUserInfo.StuEndTime
                                ExamState.setText("");
                                LeaveTime.setText("00s");
                                NextStuExamNumber.setText("");
                            }

                        } catch (Exception eJson) {
                            chronometer.stop();
                            chronometer2.setBase(SystemClock.elapsedRealtime());
                            // 开始记时
                            chronometer2.start();
                            System.out.println(eJson);

                        }
                        /////

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode== RESULT_OK) {
                    //给Image赋值,显示图片
                    try {
                       // FileInputStream fis = new FileInputStream(imageFile.getPath());
                        Bundle bundle = data.getExtras(); //data为B中回传的Intent
                        imagePath=bundle.getString("imagePath");
                        Uri imageUri = Uri.parse(imagePath);
                        if(!imagePath.equals("")) {
                            imgView2.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                           // cropImageUri(imageUri, 500, 600);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //else {
                    //Toast.makeText(this,"拍照出错，未获取到图片",Toast.LENGTH_LONG).show();
                //}
                break;
           // case CROP_IMAGE_REQUEST_CODE:
               // if(imagePath != null){
                   // imgView2.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                   // }
               // break;
            default:
                break;
        }
    }
    //上传拍的照片
    private void uploadPicture()
    {
        if(examUserInfo.CurrentUID==null)
        {
            AlertMessage("不存在学生信息。");
            return;
        }
        if(imagePath==null || imagePath.equals(""))
        {
            AlertMessage("请先拍照再上传。");
            return;
        }
        String url="http://";
        url=url+BaseUrl+"/AppDataInterface/ExamInfoShow.aspx/SaveCurrentUserPhoto";
        if(imagePath!=null) {
                Ion.with(this)
                        .load(url)
                        .setMultipartParameter("RoomName", roomname)
                        .setMultipartParameter("UID", examUserInfo.CurrentUID)
                        .setMultipartFile("image", new File(imagePath))
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (e != null) {
                                    return;
                                }
                                //////
                                try {
                                    JSONObject obj = new JSONObject(result.toString());
                                    if (result.toString().indexOf("IsSave") != -1) {
                                        String res = obj.getString("IsSave");
                                        if (res.equals("1")) {
                                            //如果上传成功则填充上面图片
                                            //AlertMessage("上传图片数据成功。");
                                            imgView2.setImageResource(R.drawable.studentimg2);
                                            imagePath=null;
                                            imgView1.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                                        } else {
                                            AlertMessage("上传图片数据失败。");
                                        }
                                    } else if (result.toString().indexOf("hasPhoto") != -1) {
                                        AlertMessage("已存在一张图片。");
                                    } else {
                                        AlertMessage("上传图片数据失败。");
                                    }

                                } catch (Exception eJson) {
                                    System.out.println(eJson);
                                }
                                /////

                            }
                        });
        }
        else
        {
            AlertMessage("请先拍照再上传。");
        }
    }
    //获取当前人员照片
    //获取图片信息进行填充
    private void getCurrentUserPhoto(){
        String url="http://";
        url=url+BaseUrl+"/AppDataInterface/ExamInfoShow.aspx/GetCurrentUserPhoto?U_ID="+examUserInfo.CurrentUID;
        Ion.with(imgView1)
                     // load the url
                .load(url);
    }
    //检测系统状态
    private void getSystemState()
    {
        String url="http://";
        url=url+BaseUrl+"/AppDataInterface/ExamInfoShow.aspx/SendExamState";
        Ion.with(this)
                .load(url)
                .setBodyParameter("RoomName", roomname)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            return;
                        }
                        //////
                        try {
                            JSONObject obj = new JSONObject(result.toString());
                            if (result.toString().indexOf("UpdateState") != -1) {
                                String res = obj.getString("UpdateState");
                                if (res.equals("1")) {
                                    //如果状态为1则重新填充考站数据，重新设置计时器
                                    //页面加载完成之后，定义计时器
                                    // if(StartFlag==false)
                                    //{
                                    //StartFlag=false;
                                    GetUserInfo();
                                    GetStationInfoUTF8();
                                    // 设置开始讲时时间
                                    //chronometer.setBase(SystemClock.elapsedRealtime());
                                    // 开始记时
                                    //chronometer.start();
                                    //StartFlag=true;
                                    //}
                                }
                            }
                        } catch (Exception eJson) {
                            System.out.println(eJson);

                        }
                        /////

                    }
                });
    }
    private void AlertMessage(String strMsg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示").setMessage(strMsg)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                //按钮事件
                            }
                        })
                .show();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        //如果服务器IP和房间设置成功才调用加载数据的方法
        if(BaseUrl!=null && roomname!=null)
        {
            //getSystemState();
            //页面加载时
            GetUserInfo();
            GetStationInfoUTF8();
        }
        else {
            Toast.makeText(getApplicationContext(), "请先设置服务器IP和房间",
                    Toast.LENGTH_LONG).show();
        }
    }


}
