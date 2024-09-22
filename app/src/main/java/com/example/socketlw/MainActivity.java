package com.example.socketlw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    private LayoutInflater inflater;
    private View layout;
    private AlertDialog.Builder builder;
    private TextView titleview;
    private ListView showmsg;
    private EditText sendmsgtext;
    private Button startserver;
    private Button continueserver;
    private Button sendmsgbt;
    private Button sendimg;
    private int StartPort;
    private boolean isContinue = true,isServer = false;
    private String message = "",userSendMsg = "",titletext = "";
    private String[] ContinueServerData = new String[2];// 0.ipv4 1.端口号
    private Long mID = 0L;
    private List<MessageInfor> datas = new ArrayList<MessageInfor>();
    private SimpleDateFormat simpleDateFormat;
    private MessageAdapte messageAdapte;
    private static Socket socket = null;//用于与服务端通信的Socket
    private static ServerSocket server;
    private static List<PrintWriter> allOut; //存放所有客户端的输出流的集合，用于广播

    private static final int IMAGE = 1;//调用系统相册-选择图片
    private static String[] PERMISSIONS_STORAGE = {
            //依次权限申请
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * 作者 LinOwl
     * 2021.02.17
     */

    @Override//实现了一个 Android Activity 的初始化逻辑
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();//隐藏标题栏
        applypermission();
        InitView();
        handler = new Handler(){//Handler 可以用来在不同线程之间发送和处理消息，更新 UI 等
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == 1){
                    titleview.setText(titletext);
                }else if(msg.what == 2){
                    titleview.setText("当前在线人数["+(allOut.size()+1)+"]");
                }
                super.handleMessage(msg);
            }
        };

    }

    /**
     * 初始化控件
     */
    private void InitView() {
        titleview = (TextView) findViewById(R.id.titleview);
        showmsg = (ListView) findViewById(R.id.showmsg);
        sendmsgtext = (EditText) findViewById(R.id.sendmsgtext);
        startserver = (Button) findViewById(R.id.startserver);
        continueserver = (Button) findViewById(R.id.continueserver);
        sendmsgbt = (Button) findViewById(R.id.sendmsgbt);
        sendimg = (Button) findViewById(R.id.sendimg);

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        messageAdapte = new MessageAdapte();
        showmsg.setAdapter(messageAdapte);

        startserver.setOnClickListener(this);
        continueserver.setOnClickListener(this);
        sendmsgbt.setOnClickListener(this);
        sendimg.setOnClickListener(this);

    }

    //定义判断权限申请的函数，在onCreat中调用就行
    public void applypermission(){
        if(Build.VERSION.SDK_INT>=23){
            boolean needapply=false;
            for(int i=0;i<PERMISSIONS_STORAGE.length;i++){
                int chechpermission= ContextCompat.checkSelfPermission(getApplicationContext(),
                        PERMISSIONS_STORAGE[i]);
                if(chechpermission!= PackageManager.PERMISSION_GRANTED){
                    needapply=true;
                }
            }
            if(needapply){
                ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE,1);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startserver:
                //加载布局
                inflater = LayoutInflater.from(this);
                layout = inflater.inflate(R.layout.start_server,null);
                //通过对 AlertDialog.Builder 对象调用 setView()
                builder =  new AlertDialog.Builder(MainActivity.this);
                builder.setView(R.layout.start_server);
                builder.setCancelable(false);//是否为可取消
                //加载控件
                EditText editprot = (EditText) layout.findViewById(R.id.editprot);

                new AlertDialog.Builder(MainActivity.this)
                        .setView(layout)  //设置显示内容
                        .setPositiveButton("开启", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StartPort = Integer.valueOf(editprot.getText().toString());
                                mID = System.currentTimeMillis();
                                ServerInit();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)  //按回退键不可取消该对话框
                        .show();
                break;
            case R.id.continueserver:
                //加载布局
                inflater = LayoutInflater.from(this);
                layout = inflater.inflate(R.layout.continue_server,null);
                //通过对 AlertDialog.Builder 对象调用 setView()
                builder =  new AlertDialog.Builder(MainActivity.this);
                builder.setView(R.layout.continue_server);
                builder.setCancelable(false);//是否为可取消
                //加载控件
                EditText editipv4text = (EditText) layout.findViewById(R.id.editipv4text);
                EditText editprottext = (EditText) layout.findViewById(R.id.editprottext);

                new AlertDialog.Builder(MainActivity.this)
                        .setView(layout)  //设置显示内容
                        .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContinueServerData[0] = editipv4text.getText().toString();
                                ContinueServerData[1] = editprottext.getText().toString();
                                ContinueSever();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)  //按回退键不可取消该对话框
                        .show();

                break;
            case R.id.sendmsgbt://发送消息
                if(isServer){//服务器
                    message = sendmsgtext.getText().toString();
                    if(message==null||"".equals(message)){
                        Toast.makeText(MainActivity.this,"发送消息不能为空",Toast.LENGTH_LONG).show();
                        return ;
                    }
                    long Ltimes = System.currentTimeMillis();
                    String signature="暂时签名";
                    message = sendmsgtext.getText().toString();
                    datas.add(new MessageInfor(message,Ltimes,mID,signature,"1"));
                    sendMessage("{\"isimg\":\"1\",\"msg\":\""+message+"\",\"times\":\""+Ltimes+"\",\"id\":\""+mID+"\",\"signature\":\""+signature+"\",\"peoplen\":\""+"当前在线人数["+(allOut.size()+1)+"]"+"\"}");
                    sendmsgtext.setText("");



                }else {//客户端
                    sendMsgText();
                }


                break;
            case R.id.sendimg:
                //调用相册
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
                break;
        }
    }

    /**
     * 服务器端
     * @param out
     */
    //将给定的输出流放入集合
    private synchronized void addOut(PrintWriter out){
        allOut.add(out);
    }

    //将给定的输出流移出集合
    private synchronized void removeOut(PrintWriter out){
        allOut.remove(out);
    }


    //将给定的消息发给客户端
    private void sendMessage(String message) {
        Thread sendmsg = new Thread(new Runnable() {
            @Override
            public void run() {
                for(PrintWriter out:allOut) {
                    out.println(message);
                }
            }
        });
        sendmsg.start();
    }


    //服务器初始化
    public void ServerInit() {
        try {
            server = new ServerSocket(StartPort);
            allOut = new ArrayList<PrintWriter>();
            isServer = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // 无限循环监听客户端连接，所以才要整一个新线程来防止阻碍主线程
                        while(true) {
                            Socket socket1 = null;
                            try {
                                socket1 = server.accept();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            // 当有客户端连接时，创建一个新的ClientHandler实例来处理该客户端
                            ClientHandler hander = new ClientHandler(socket1);

                            // 为每个客户端启动一个新线程来执行ClientHandler
                            Thread t = new Thread(hander);
                            t.start();
                        }
                    }
                }).start();
    }


    //该线程类是与指定的客户端进行交互工作
    class ClientHandler implements Runnable{
        //当前线程客户端的Socket
        private Socket socket;

        //该客户端的地址
        private String host;

        public ClientHandler(Socket socket) {
            this.socket=socket;
            InetAddress address = socket.getInetAddress();
            //获取ip地址
            host = address.getHostAddress();
        }

        @Override
        public void run() {
            PrintWriter pw = null;
            try {
                //有用户加入
                sendMessage("["+host+"]加入聊天!");

                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");
                pw = new PrintWriter(osw,true);

                //将该客户的输出流存入共享集合，以便消息可以广播给该客户端
                addOut(pw);

                handler.sendEmptyMessage(2);

                //处理来自客户端的数据
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in,"utf-8");
                BufferedReader br = new BufferedReader(isr);

                String message = null;
                while((message = br.readLine())!=null) {

                    try {//接收到客户端发送的消息后，使用 JSONObject 解析消息。
                        JSONObject json = new  JSONObject(message);
                        if(json.getString("isimg").equals("1")){//不为图片
                            datas.add(new MessageInfor(json.getString("msg"),Long.valueOf(json.getString("times")),Long.valueOf(json.getString("id")),String.valueOf(json.getString("signature")),"1"));
                        }else if(json.getString("isimg").equals("0")){//为图片
                            datas.add(new MessageInfor(json.getString("msg"),Long.valueOf(json.getString("times")),Long.valueOf(json.getString("id")),String.valueOf(json.getString("signature")),"0"));
                        }
                        titletext = json.getString("peoplen");
                        handler.sendEmptyMessage(1);
                        //messageAdapte.notifyDataSetChanged();//通知数据源发生变化
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    //将解析后的消息广播给所有客户端
                    sendMessage(message);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally {
                //将该客户端的输出流从共享集合中删除，以避免后续广播给已断开的客户端
                removeOut(pw);

                //有用户退出
                sendMessage("["+host+"]退出聊天!");

                handler.sendEmptyMessage(2);

                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 客户端
     * @return
     */
    public boolean ContinueSever(){

        Thread continuethread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //localhost 127.0.0.1
                            socket = new Socket(ContinueServerData[0],Integer.valueOf(ContinueServerData[1]));
                            mID = System.currentTimeMillis();
                        } catch (Exception e) {
                            isContinue = false;
                            isServer = false;
                            e.printStackTrace();
                        }
                    }
                }
        );
        continuethread.start();

        while(isContinue){
            if(socket != null){
                break;
            }
        }


        if(isContinue) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            /*
                             * 客户端开始工作的方法
                             */
                            try {
                                //启动用于读取服务端发送消息的线程
                                ServerHandler handler = new ServerHandler();
                                //ServerHandler是自己写的类，实现Runnable接口,有多线程功能
                                Thread t = new Thread(handler);
                                t.start();

                                //将数据发送到服务端
                                OutputStream out = socket.getOutputStream();//获取输出流对象
                                OutputStreamWriter osw = new OutputStreamWriter(out,"utf-8");//转化成utf-8格式
                                PrintWriter pw = new PrintWriter(osw,true);
                                while(true) {
                                    if(userSendMsg != "" && userSendMsg!=null){
                                        pw.println(userSendMsg);//把信息输出到服务端
                                        userSendMsg = "";
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
        }
        return isContinue;
    }

    class ServerHandler implements Runnable{
        /**
         * 读取服务端发送过来的消息
         */
        @Override
        public void run() {
            try {
                InputStream in = socket.getInputStream();//输入流
                InputStreamReader isr = new InputStreamReader(in,"UTF-8");//以utf-8读
                BufferedReader br = new BufferedReader(isr);
                String message1=br.readLine();
                while(message1!=null) {
                    Log.i("测试4",message1);
                    try {
                        JSONObject json = new  JSONObject(message1);
                        if(json.getLong("id") != mID){
                            if(json.getString("isimg").equals("1")){//不为图片
                                datas.add(new MessageInfor(json.getString("msg"),Long.valueOf(json.getString("times")),Long.valueOf(json.getString("id")),String.valueOf(json.getString("signature")),"1"));
                            }else if(json.getString("isimg").equals("0")){//为图片
                                datas.add(new MessageInfor(json.getString("msg"),Long.valueOf(json.getString("times")),Long.valueOf(json.getString("id")),String.valueOf(json.getString("signature")),"0"));
                            }
                        }
                        titletext = json.getString("peoplen");
                        handler.sendEmptyMessage(1);
                        //messageAdapte.notifyDataSetChanged();//通知数据源发生变化
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    message1=br.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送消息
     */
    private void sendMsgText(){
        message = sendmsgtext.getText().toString();
        if(message==null||"".equals(message)){
            Toast.makeText(MainActivity.this,"发送消息不能为空",Toast.LENGTH_LONG).show();
            return ;
        }
        long Ltimes = System.currentTimeMillis();
        String signature="暂时签名";
        MessageInfor m = new MessageInfor(message,Ltimes,mID,signature,"1");//消息 时间戳 id
        userSendMsg = "{\"isimg\":\"1\",\"msg\":\""+sendmsgtext.getText().toString()+"\",\"times\":\""+Ltimes+"\",\"signature\":\""+signature+"\",\"id\":\""+mID+"\"}";
        datas.add(m);
        messageAdapte.notifyDataSetChanged();//通知数据源发生变化

        sendmsgtext.setText("");
    }

    class MessageAdapte extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public MessageInfor getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            Long id = datas.get(i).getUserID();
            return id==null?0:id;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            MessageHolder holder = null;
            if(view == null){
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.chart_item,null);
                holder = new MessageHolder();
                holder.left = (TextView) view.findViewById(R.id.itemleft);
                holder.right = (TextView) view.findViewById(R.id.itemright);

                holder.leftsign = (TextView) view.findViewById(R.id.itemsignleft);
                holder.rightsign = (TextView) view.findViewById(R.id.itemsignright);
                holder.lefttime = (TextView) view.findViewById(R.id.itemtimeleft);
                holder.righttime = (TextView) view.findViewById(R.id.itemtimeright);

                holder.rightimgtime = (TextView) view.findViewById(R.id.rightimgtime);
                holder.leftimgtime = (TextView) view.findViewById(R.id.leftimgtime);
                holder.rightimg = (ImageView) view.findViewById(R.id.rightimg);
                holder.leftimg = (ImageView) view.findViewById(R.id.leftimg);

                view.setTag(holder);
            }else {
                holder = (MessageHolder) view.getTag();
            }
            MessageInfor mi = getItem(i);
            //显示
            if (mi.getUserID() == mID){//id相等
                if(mi.getType().equals("0")){//图片
                    holder.leftimg.setVisibility(View.GONE);
                    holder.leftimgtime.setVisibility(View.GONE);

                    holder.rightimg.setVisibility(View.VISIBLE);
                    holder.rightimgtime.setVisibility(View.VISIBLE);

                    holder.rightimg.setImageBitmap(convertStringToIcon(mi.getMsg()));
                    holder.rightimgtime.setText(simpleDateFormat.format(new Date(mi.getTime())));

                    holder.left.setVisibility(View.GONE);
                    holder.lefttime.setVisibility(View.GONE);
                    holder.right.setVisibility(View.GONE);
                    holder.righttime.setVisibility(View.GONE);

                }else if(mi.getType().equals("1")){//消息
                    holder.leftimg.setVisibility(View.GONE);
                    holder.leftimgtime.setVisibility(View.GONE);
                    holder.rightimg.setVisibility(View.GONE);
                    holder.rightimgtime.setVisibility(View.GONE);


                    holder.left.setVisibility(View.GONE);
                    holder.lefttime.setVisibility(View.GONE);
                    holder.leftsign.setVisibility(View.GONE);
                    holder.right.setVisibility(View.VISIBLE);
                    holder.righttime.setVisibility(View.VISIBLE);
                    holder.rightsign.setVisibility(View.VISIBLE);
                    holder.right.setText(mi.getMsg());
                    holder.righttime.setText(simpleDateFormat.format(new Date(mi.getTime())));
                    holder.rightsign.setText(mi.getSignature());
                }


            }else {//对面发的
                if(mi.getType().equals("0")){//图片
                    holder.leftimg.setVisibility(View.VISIBLE);
                    holder.leftimgtime.setVisibility(View.VISIBLE);
                    holder.rightimg.setVisibility(View.GONE);
                    holder.rightimgtime.setVisibility(View.GONE);
                    holder.leftimg.setImageBitmap(convertStringToIcon(mi.getMsg()));
                    holder.leftimgtime.setText(simpleDateFormat.format(new Date(mi.getTime())));

                    holder.left.setVisibility(View.GONE);
                    holder.lefttime.setVisibility(View.GONE);
                    holder.right.setVisibility(View.GONE);
                    holder.righttime.setVisibility(View.GONE);

                }else if(mi.getType().equals("1")){//消息
                    holder.leftimg.setVisibility(View.GONE);
                    holder.leftimgtime.setVisibility(View.GONE);
                    holder.rightimg.setVisibility(View.GONE);
                    holder.rightimgtime.setVisibility(View.GONE);

                    holder.left.setVisibility(View.VISIBLE);
                    holder.lefttime.setVisibility(View.VISIBLE);
                    holder.leftsign.setVisibility(View.VISIBLE);
                    holder.right.setVisibility(View.GONE);
                    holder.righttime.setVisibility(View.GONE);
                    holder.rightsign.setVisibility(View.GONE);
                    holder.left.setText(mi.getMsg());
                    holder.lefttime.setText(simpleDateFormat.format(new Date(mi.getTime())));
                    holder.leftsign.setText(mi.getSignature());
                }
            }
            return view;
        }
    }

    class MessageHolder{
        public TextView left;
        public TextView right;
        public TextView lefttime;
        public TextView righttime;
        public TextView leftsign;
        public TextView rightsign;



        private TextView rightimgtime;
        private TextView leftimgtime;
        private ImageView rightimg;
        private ImageView leftimg;

    }
    //获取图片路径
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {


        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);

            String imagePath = c.getString(columnIndex);

            activityImage(imagePath);
            c.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 处理图片发送
     * @param imaePath 图片路径
     */
    private void activityImage(String imaePath){

        Bitmap bm = BitmapFactory.decodeFile(imaePath);
        bm =  resizeBitmap(bm,400,400,true);
        long Ltimes = System.currentTimeMillis();
        String imgString = convertIconToString(bm);
        imgString = imgString.replace("\n","");
        String signature="暂时签名" ;
        datas.add(new MessageInfor(imgString,Ltimes,mID,signature,"0"));

        if(isServer){//服务器
            sendMessage("{\"isimg\":\"0\",\"msg\":\""+imgString+"\",\"times\":\""+Ltimes+"\",\"signature\":\""+signature+"\",\"id\":\""+mID+"\"}");
        }else {//客户端
            userSendMsg = "{\"isimg\":\"0\",\"msg\":\""+imgString+"\",\"times\":\""+Ltimes+"\",\"signature\":\""+signature+"\",\"id\":\""+mID+"\"}";
        }


    }


    /**
     * 图片转成string
     * @param bitmap
     * @return
     */
    private String convertIconToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }

    /**
     * string转成bitmap
     * @param st
     * @return
     */
    private Bitmap convertStringToIcon(String st){
        Bitmap bitmap = null;
        try
        {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,bitmapArray.length);
            return bitmap;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 处理图片
     * @param bitmap 图片bitmap
     * @param MaxWidth 最大长
     * @param MaxHeight 最大宽
     * @param filter 是否过滤
     * @return 处理后的bitmap
     */
    private Bitmap resizeBitmap(Bitmap bitmap,int MaxWidth,int MaxHeight,boolean filter){
        Float ScalingNumber;
        Bitmap reBitmap;
        Matrix matrix = new Matrix();
        ScalingNumber = Float.valueOf(scalingNumber(bitmap.getWidth(),bitmap.getHeight(),MaxWidth,MaxHeight));
        matrix.setScale(1/ScalingNumber, 1/ScalingNumber);
        reBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, filter);

        return reBitmap;
    }

    /**
     * 计算缩放比例
     * @param oldWidth 原长
     * @param oldHeight 原宽
     * @param MaxWidth 最大长
     * @param MaxHeight 最大宽
     * @return 缩放比系数
     */
    private int scalingNumber(int oldWidth,int oldHeight,int MaxWidth,int MaxHeight){
        int scalingN = 1;
        if(oldWidth > MaxWidth || oldHeight > MaxHeight){
            scalingN = 2;
            while((oldWidth/scalingN > MaxWidth) || (oldHeight/scalingN > MaxHeight)){
                scalingN*=2;
            }
        }

        return scalingN;
    }



}
























