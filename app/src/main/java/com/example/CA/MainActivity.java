package com.example.CA;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import android.view.View;

import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStreamReader;

//http请求用的
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;

import com.android.volley.VolleyError;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView showres;
    private Button deployContractButton;
    private Button publishPublicKeyButton;
    private Button updatePublicKeyButton;
    private Button batchRegisterButton;
    private Button showFile;
    private Button revokePkButton;

    //private String address = "0xccdee8c8017f64c686fa39c42f883f363714e078";
    private String address = "0x4f4072fc87a0833ea924f364e8a2af3546f71279";//地址2

    //http参数
    private String RegpublicKey;

    //部署合约
    private  String urlDeployContract = "http://192.168.220.20:8080/deployContract";
    //更新公钥
    private  String urlPkDerive = "http://192.168.220.20:8080/pkDerive";
    //注册公钥
    private  String urlBatchRegisterPk =  "http://192.168.220.20:8080/BatchRegisterPk";
    //批量注册公钥
    private  String urlRegisterPk =  "http://192.168.220.20:8080/registerPk";
    //撤销公钥
    private  String urlRevokePk =  "http://192.168.220.20:8080/revokePk";

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

        //清空文件内容
        clearFileOnStartup();
    }
    /**
     * 初始化控件
     */
    private void InitView() {

        showres = (TextView) findViewById(R.id.showres);
        showres.setMovementMethod(new ScrollingMovementMethod());

        deployContractButton = (Button) findViewById(R.id.deployContractButton);
        updatePublicKeyButton = (Button) findViewById(R.id.updatePublicKeyButton);
        batchRegisterButton = (Button) findViewById(R.id.batchRegisterButton);
        showFile = (Button) findViewById(R.id.showfile);
        publishPublicKeyButton = (Button) findViewById(R.id.publishPublicKeyButton);
        revokePkButton = (Button) findViewById(R.id.revokePkButton);


        deployContractButton.setOnClickListener(this);
        updatePublicKeyButton.setOnClickListener(this);
        batchRegisterButton.setOnClickListener(this);
        publishPublicKeyButton.setOnClickListener(this);
        revokePkButton.setOnClickListener(this);
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
            case R.id.deployContractButton://部署公钥
                getOne(urlDeployContract);
                break;
            case R.id.updatePublicKeyButton://更新公钥
                postOne(urlPkDerive);
                break;
            case R.id.batchRegisterButton://批量注册公钥
                postOne(urlBatchRegisterPk);
                break;
            case R.id.publishPublicKeyButton://注册公钥
                postOne(urlRegisterPk);
                break;
            case R.id.revokePkButton://撤销公钥
                postOne(urlRevokePk);
                break;
            case R.id.showfile://展示日志
                showFileContentDialog();
                break;
            default:
        }
    }

//传1个参数
private void postOne(String url) {
    RequestQueue queue = Volley.newRequestQueue(this);
    StringRequest jsonRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                   // res=response;
                    Log.d("测试PostOne", "url = " + url);
                    Log.d("测试PostOne", "response =" + response);

                    switch (url){
                        case "http://192.168.220.20:8080/pkDerive":
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String runtime = jsonObject.getString("runtime");
                                String chain = jsonObject.getString("chain");
                                String publicKey = jsonObject.getString("publicKey");
                                RegpublicKey=publicKey;
                                // 输出或者使用这些数据
                                showres.setText("SM2_PublicKeyDerive duration:"+runtime+"\npublicKey="+publicKey+"\nchain="+chain);
                                writeToInternalStorage("-------SM2_PublicKeyDerive-------");
                                writeToInternalStorage("SM2_PublicKeyDerive duration:"+runtime+"\npublicKey="+publicKey+"\nchain="+chain);
                                writeToInternalStorage("\n");
                            } catch (JSONException e) {
                                Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                            }
                            break;
                        case "http://192.168.220.20:8080/registerPk":
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String runtime = jsonObject.getString("runtime");
                                String message = "“成功注册公钥:生成公钥证书,将公钥证书嵌入到交易发布到区块链上,更新合约内容”";
                                String publicKey = jsonObject.getString("publicKey");
                                String transaction = jsonObject.getString("transaction");

                                showres.setText("Register_PublicKey duration:"+runtime+"\nmessage="+message+"\npublicKey="+publicKey+"\ntransaction="+transaction);
                                writeToInternalStorage("-------Register_PublicKey-------");
                                writeToInternalStorage("Register_PublicKey duration:"+runtime+"\nmessage="+message+"\npublicKey="+publicKey+"\ntransaction="+transaction);
                                writeToInternalStorage("\n");
                            } catch (JSONException e) {
                                Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                            }
                            break;
                        case "http://192.168.220.20:8080/BatchRegisterPk":
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String runtime = jsonObject.getString("runtime");
                                String message = "“成功派生密钥五次，并注册公钥”";
                                String publicKey = jsonObject.getString("publicKey");
                                String transaction = jsonObject.getString("transaction");

                                showres.setText("BatchRegisterPk duration:"+runtime+"\nmessage="+message+"\npublicKey="+publicKey+"\ntransaction="+transaction);
                                writeToInternalStorage("----------BatchRegisterPk--------");
                                writeToInternalStorage("BatchRegisterPk duration:"+runtime+"\nmessage="+message+"\npublicKey="+publicKey+"\ntransaction="+transaction);
                                writeToInternalStorage("\n");
                            } catch (JSONException e) {
                                Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                            }
                            break;
                        case "http://192.168.220.20:8080/revokePk":
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String runtime = jsonObject.getString("runtime");
                                String message = jsonObject.getString("message");

                                showres.setText("Revoke_PublicKey duration:"+runtime+"\n撤销是否成功:"+message+"\n被撤销的publickey="+RegpublicKey);
                                writeToInternalStorage("----------Revoke_PublicKey-------");
                                writeToInternalStorage("Revoke_PublicKey duration:"+runtime+"\n撤销是否成功:"+message+"\n被撤销的publickey="+RegpublicKey);
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
                case "http://192.168.220.20:8080/deployContract":
                    break;
                case "http://192.168.220.20:8080/pkDerive"://给地址返回
                    jsonObject=JsonPut.PutJson(jsonObject,"address",address);
                    break;
                case "http://192.168.220.20:8080/registerPk":
                    jsonObject=JsonPut.PutJson(jsonObject,"address",address);
                    break;
                case "http://192.168.220.20:8080/BatchRegisterPk":
                    jsonObject=JsonPut.PutJson(jsonObject,"address",address);
                    break;
                case "http://192.168.220.20:8080/revokePk":
                    jsonObject=JsonPut.PutJson(jsonObject,"key",RegpublicKey);
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

//get方法
private void getOne(String url) {
    RequestQueue queue = Volley.newRequestQueue(this);
    StringRequest jsonRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("测试PostOne", "url = " + url);
                    Log.d("测试PostOne", "response =" + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String runtime = jsonObject.getString("runtime");
                        String message = "合约部署成功，合约地址="+address;
                        showres.setText("MapPkToTx.deploy duration:"+runtime+"\n"+message);
                        writeToInternalStorage("----------DeployContract---------");
                        writeToInternalStorage("MapPkToTx.deploy duration:"+runtime+"\n"+message);
                        writeToInternalStorage("\n");
                    } catch (JSONException e) {
                        Log.e("JSON解析错误", "解析失败: " + e.getMessage());
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
        public String getBodyContentType() {
            return "application/json; charset=utf-8";
        }
    };
    int socketTimeout = 10000; // 10 seconds
    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    jsonRequest.setRetryPolicy(policy);


    queue.add(jsonRequest);
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
