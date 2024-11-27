package com.example.CA;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//http请求用的
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;

import com.android.volley.VolleyError;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.os.Handler;
import android.os.Looper;
import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView showres;

    private Button deployContractButton;
    private Button traceuserButton;
    private Button showFile;


    private String address1 = "0xccdee8c8017f64c686fa39c42f883f363714e078";
    private String address2 = "0x4f4072fc87a0833ea924f364e8a2af3546f71279";//地址2

    private String token="0x4c26aecee34487d29adff978fd6791578ed8fd28";
    //http参数
    private String RegpublicKey="null";
    private String testPk="9A35281B56A83C6DDF86453BB7FA9F3FB0BA4EFBAEB694C79E0EC2C1D2364435DDEE3DDB9DF033AAA6E0691514FD3B0D5C13A78CCD0BCF5ED854D214C646B130";
    private String testchain="560AF94CC1C8BB9AE6986502136B425D";
    //部署合约
    private  String urlDeployContract = "http://110.41.188.6:8080/deployContract";
    //初始化请求
    private  String urlInitialuser = "http://110.41.188.6:8080/getuser";
    //追踪用户
    private  String urlTraceuser = "http://110.41.188.6:8080/traceuser";
    //更新公钥
    private  String urlPkDerive = "http://110.41.188.6:8080/pkDerive";
    //注册公钥
    private  String urlBatchRegisterPk =  "http://110.41.188.6:8080/BatchRegisterPk";
    //批量注册公钥
    private  String urlRegisterPk =  "http://110.41.188.6:8080/registerPk";
    //撤销公钥
    private  String urlRevokePk =  "http://110.41.188.6:8080/revokePk";

    private Handler handler;
    private Runnable runnable;
    private ExecutorService executorService;
    private List<ListItem> mdata= new ArrayList<>();;
    private static String[] PERMISSIONS_STORAGE = {
            //依次权限申请
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override//实现了一个 Android Activity 的初始化逻辑
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();//隐藏标题栏
        applypermission();
        InitView();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //添加数据项
        List<ListItem> data = new ArrayList<>();
        data.add(new ListItem(R.drawable.man, address1, "",
                "", 0,"user1"));
        data.add(new ListItem(R.drawable.woman, address2, "", "", 0,"user2"));

        MyAdapter adapter = new MyAdapter(data, this,showres);
        recyclerView.setAdapter(adapter);
        mdata=data;

       // postOne(urlInitialuser, 0);

      //  executorService = Executors.newSingleThreadExecutor();
       // startBackgroundTask();
        //清空文件内容
        clearFileOnStartup();
    }

    /**
     * 初始化控件
     */
    private void InitView() {

        showres = (TextView) findViewById(R.id.showres);
        showres.setMovementMethod(new ScrollingMovementMethod());//能划

        deployContractButton = (Button) findViewById(R.id.deployContractButton);
        traceuserButton = (Button) findViewById(R.id.traceuserButton);
        showFile = (Button) findViewById(R.id.showfile);


        deployContractButton.setOnClickListener(this);
        traceuserButton.setOnClickListener(this);
        showFile.setOnClickListener(this);
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
            case R.id.deployContractButton://部署合约
                postOne(urlDeployContract,-1);
                break;
            case R.id.traceuserButton://追踪用户
                postOne(urlTraceuser,-1);
                break;
            case R.id.showfile://展示日志
                showFileContentDialog();
                break;
            default:
        }
    }

//传1个参数
private void postOne(String url,int position) {
    RequestQueue queue = Volley.newRequestQueue(this);
    StringRequest jsonRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                   // res=response;
                    Log.d("测试PostOne", "url = " + url);
                    Log.d("测试PostOne", "response =" + response);

                    switch (url){
                        case "http://110.41.188.6:8080/initial":
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String chain = jsonObject.getString("chain");
                                String publicKey = jsonObject.getString("publicKey");
                                String keyIndex = jsonObject.getString("keyIndex");

                                String message= "";
                                message=jsonObject.getString("message");
                                if(!message.equals("user not found")){
                                    mdata.get(position).setPublickey(publicKey);
                                    mdata.get(position).setChain(chain);
                                    mdata.get(position).setKeyIndex(Integer.parseInt(keyIndex));
                                    mdata.get(position).setInitialized(true);
                                    // 输出或者使用这些数据
                                    showres.setText("username="+mdata.get(position).getUsername()+"\naddress="+mdata.get(position).getaddress()+"\npublicKey="+publicKey+"\nchain="+chain+"\nkeyIndex="+keyIndex);
                                    writeToInternalStorage("--------Initial--------");
                                    writeToInternalStorage("username="+mdata.get(position).getUsername()+"\naddress="+mdata.get(position).getaddress()+"\npublicKey="+publicKey+"\nchain="+chain+"\nkeyIndex="+keyIndex);
                                    writeToInternalStorage("\n");
                                }else{

                                }
                            } catch (JSONException e) {
                                Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                            }
                            break;
                        case "http://110.41.188.6:8080/deployContract":
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String runtime = jsonObject.getString("runtime");
                                String message = "合约部署成功";
                                showres.setText("MapPkToTx.deploy duration:"+runtime+"\n"+message);
                                writeToInternalStorage("----------DeployContract---------");
                                writeToInternalStorage("MapPkToTx.deploy duration:"+runtime+"\n"+message);
                                writeToInternalStorage("\n");
                            } catch (JSONException e) {
                                Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                            }
                            break;

                        case "http://110.41.188.6:8080/traceuser":
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                               // String runtime = jsonObject.getString("runtime");
                                String message = jsonObject.getString("message");;
                                showres.setText("trackUser message:"+message);
                                writeToInternalStorage("------------TrackUser-----------");
                                writeToInternalStorage("\nmessage:"+message);
                                writeToInternalStorage("\n");
                            } catch (JSONException e) {
                                Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                            }
                            break;

                        default:
                            System.out.println("Url错误！！");

                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null) {
                        Log.e("TAG", "Response code: " + error.networkResponse.statusCode);
                        Log.e("TAG", "Response data: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("TAG", "Network response is null");
                    }
                    String errorMessage = error.getMessage();
                    if (errorMessage == null) {
                        Log.e("TAG", "Error message is null");
                    } else {
                        Log.e("TAG", "Error message: " + errorMessage);
                    }

                }
            }) {
        @Override
        public byte[] getBody() {
            JSONObject jsonObject = new JSONObject();
            switch (url){
                case "http://110.41.188.6:8080/initial":
                    jsonObject=JsonPut.PutJson(jsonObject,"token",token);
                    jsonObject=JsonPut.PutJson(jsonObject,"address",mdata.get(position).getaddress());
                    break;
                case "http://110.41.188.6:8080/deployContract":
                    jsonObject=JsonPut.PutJson(jsonObject,"token",token);
                    break;
                case "http://110.41.188.6:8080/traceuser":
                    if(mdata.get(1).getRegpublicKey().equals("")){
                        RegpublicKey=mdata.get(0).getRegpublicKey();
                    }else{
                        RegpublicKey=mdata.get(1).getRegpublicKey();
                    }
                    jsonObject=JsonPut.PutJson(jsonObject,"key",RegpublicKey);
                    jsonObject=JsonPut.PutJson(jsonObject,"token",token);
                    break;
                default:
                    System.out.println("Url错误！！");
                    return new byte[0]; // 返回空字节数组
            }
            System.out.println(jsonObject.toString());
            return jsonObject.toString().getBytes();

        }
        @Override
        public String getBodyContentType() {
            return "application/json; charset=utf-8";
        }
    };
    int socketTimeout = 10000; // 10 seconds
    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    jsonRequest.setRetryPolicy(policy);
    queue.add(jsonRequest);
}

    private void startBackgroundTask() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean allInitialized = false;

                while (!allInitialized) {
                    for (int i=0;i<mdata.size();i++) {
                        if (!mdata.get(i).isInitialized()) {
                            postOne(urlInitialuser, i);
                        }
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 检查所有 ListItem 是否都已初始化
                    allInitialized = true;
                    for (ListItem checkItem : mdata) {
                        if (!checkItem.isInitialized()) {
                            allInitialized = false;
                            break;
                        }
                    }
                }

                // 任务完成后关闭线程池
                executorService.shutdown();
            }
        });
    }
    // 写入文件到内部存储
    private void writeToInternalStorage(String data) {
        try (FileOutputStream fos = openFileOutput("logg.txt", MODE_APPEND)) {
            fos.write(data.getBytes());
            fos.write("\n".getBytes()); // 添加换行符，确保每行数据占一行
            Log.d("logg", "File written to internal storage.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", "Error writing file to internal storage: " + e.getMessage());
        }
    }

    //清空logg文件
    private void clearFileOnStartup() {
        try (FileOutputStream fos = openFileOutput("logg.txt", MODE_PRIVATE)) {
            // 写入空字符串以清空文件内容
            fos.write("".getBytes());
            Log.d("MyFile", "File cleared on startup.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", "Error clearing file on startup: " + e.getMessage());
        }
    }
    //展示文件内容
    private void showFileContentDialog() {
        // 读取文件内容
        String fileContent = readFromInternalStorage();

        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("文件内容");
        builder.setMessage(fileContent);

        // 添加按钮
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // 添加"复制"按钮
        builder.setNegativeButton("复制", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取剪贴板服务
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("文件内容", fileContent);
                clipboard.setPrimaryClip(clip);  // 设置剪贴板内容
                Toast.makeText(getApplicationContext(), "内容已复制", Toast.LENGTH_SHORT).show();  // 提示用户
            }
        });
        // 显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //读取文件
    private String readFromInternalStorage() {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("logg.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", "Error reading file from internal storage: " + e.getMessage());
        }
        return stringBuilder.toString();
    }

}
