package com.example.CA;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<ListItem> mData;
    private Context mContext;
    private TextView mshowres;
    private int height;
    private boolean isExpanded1 = false;
    private boolean isExpanded2 = false;
    private boolean isExpanded3 = false;
    private boolean isExpanded4 = false;


    public MyAdapter(List<ListItem> data, Context context,TextView showres) {
        this.mData = data;
        this.mContext = context;
        this.mshowres=showres;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItem item = mData.get(position);

        // 设置四行文本
        holder.textView1.setText(item.getaddress());
        holder.textView2.setText(item.getpublickey());
        holder.textView3.setText(item.getchain());
        holder.textView4.setText(String.valueOf(item.getkeyIndex()));

        // 设置左边图片
        holder.leftImage.setImageResource(item.getImageResource());
        ViewGroup.LayoutParams params2 = holder.prefix_address.getLayoutParams();
        height=params2.height;

        //地址的展开按钮
        holder.expand_button_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isExpanded1) {//110,145
                    reduceTextView(holder.textView1,view);
                } else {
                    expandTextViewToFitContent(holder.textView1,view);
                }
                isExpanded1 = !isExpanded1;
            }
        });

        //公钥的展开按钮
        holder.expand_button_pk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isExpanded2) {//110,145
                    reduceTextView(holder.textView2,view);
                } else {
                    expandTextViewToFitContent(holder.textView2,view);
                }
                isExpanded2 = !isExpanded2;
            }
        });

        //chain的展开按钮
        holder.expand_button_chain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isExpanded3) {//110,145
                    reduceTextView(holder.textView3,view);
                } else {
                    expandTextViewToFitContent(holder.textView3,view);
                }
                isExpanded3 = !isExpanded3;
            }
        });

        //keyIndex的展开按钮
        holder.expand_button_keyIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isExpanded4) {//110,145
                    reduceTextView(holder.textView4,view);
                } else {
                    expandTextViewToFitContent(holder.textView4,view);
                }
                isExpanded4 = !isExpanded4;
            }
        });

        holder.rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理按钮点击事件
                showPopupWindow(v, position, holder);
            }
        });
    }

    private void showPopupWindow(View anchorView, final int position, final ViewHolder holder) {
        // 使用 LayoutInflater 加载 popup_layout.xml
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.popup_buttons, null);

        // 获取按钮实例
        Button publishPublicKeyButton = popupView.findViewById(R.id.publishPublicKeyButton);
        Button updatePublicKeyButton = popupView.findViewById(R.id.updatePublicKeyButton);
        Button batchRegisterButton = popupView.findViewById(R.id.batchRegisterButton);
        Button revokePkButton = popupView.findViewById(R.id.revokePkButton);

        // 注册公钥按钮
        publishPublicKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理 Button 1 点击事件
                handleButton1Click(position,holder);

            }
        });

        updatePublicKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理 Button 2 点击事件
                handleButton2Click(position,holder);
            }
        });

        batchRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理 Button 3 点击事件
                handleButton3Click(position,holder);
            }
        });

        revokePkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理 Button 4 点击事件
                handleButton4Click(position,holder);
            }
        });

        // 创建 PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // 设置背景
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        // 设置点击外部区域时关闭 PopupWindow
        popupWindow.setOutsideTouchable(true);

        // 显示 PopupWindow
        popupWindow.showAsDropDown(anchorView, 0, 0);
    }

    //收起函数
    private void reduceTextView(TextView textView,View view) {
        // 计算文本内容所需的高度
        textView.post(new Runnable() {
            @Override
            public void run() {
                textView.setMaxLines(1);
                ViewGroup.LayoutParams params = textView.getLayoutParams();
                params.height = height;
                textView.setLayoutParams(params);
                ((ImageView) view).setImageResource(R.drawable.ic_expand_more);
            }
        });
    }

    //展开函数
    private void expandTextViewToFitContent(TextView textView,View view) {
        textView.setMaxLines(Integer.MAX_VALUE);
        // 计算文本内容所需的高度
        textView.post(new Runnable() {
            @Override
            public void run() {
                Layout layout = textView.getLayout();
                if (layout != null) {
                    int height = layout.getLineTop(layout.getLineCount());
                    ViewGroup.LayoutParams params = textView.getLayoutParams();
                    params.height = height+55;
                    Log.d("height=", String.valueOf(height));
                    textView.setLayoutParams(params);
                    ((ImageView) view).setImageResource(R.drawable.ic_expand_less);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView leftImage;
        public TextView textView1, textView2, textView3, textView4;
        public TextView prefix_address;
        public ImageView expand_button_address;
        public ImageView expand_button_pk;
        public ImageView expand_button_chain;
        public ImageView expand_button_keyIndex;

        public ImageView rightButton;

        public ViewHolder(View itemView) {
            super(itemView);
            leftImage = itemView.findViewById(R.id.left_image);
            textView1 = itemView.findViewById(R.id.text_line_1);
            textView2 = itemView.findViewById(R.id.text_line_2);
            textView3 = itemView.findViewById(R.id.text_line_3);
            textView4 = itemView.findViewById(R.id.text_line_4);
            prefix_address = itemView.findViewById(R.id.prefix_address);

            expand_button_address=itemView.findViewById(R.id.expand_button_address);
            expand_button_pk = itemView.findViewById(R.id.expand_button_pk);
            expand_button_chain = itemView.findViewById(R.id.expand_button_chain);
            expand_button_keyIndex = itemView.findViewById(R.id.expand_button_keyIndex);

            rightButton = itemView.findViewById(R.id.right_button);
        }
    }
    private void handleButton1Click(int position,final ViewHolder holder) {
        // 处理 Button 1 点击事件
        ListItem item = mData.get(position);
        // 执行相应操作
        postOne("http://110.41.188.6:8080/registerPk",item,holder);
    }

    private void handleButton2Click(int position,final ViewHolder holder) {
        // 处理 Button 2 点击事件
        ListItem item = mData.get(position);
        // 执行相应操作
        postOne("http://110.41.188.6:8080/pkDerive",item,holder);
    }

    private void handleButton3Click(int position,final ViewHolder holder) {
        // 处理 Button 3 点击事件
        ListItem item = mData.get(position);
        // 执行相应操作
        postOne("http://110.41.188.6:8080/BatchRegisterPk",item,holder);
    }

    private void handleButton4Click(int position,final ViewHolder holder) {
        // 处理 Button 4 点击事件
        ListItem item = mData.get(position);
        // 执行相应操作
        postOne("http://110.41.188.6:8080/revokePk",item,holder);
    }

    private void postOne(String url,ListItem item,final ViewHolder holder) {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        StringRequest jsonRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // res=response;
                        Log.d("测试PostOne", "url = " + url);
                        Log.d("测试PostOne", "response =" + response);

                        switch (url){
                            case "http://110.41.188.6:8080/deployContract":
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String runtime = jsonObject.getString("runtime");
                                    String message = "合约部署成功，合约地址="+item.getaddress();
                                    mshowres.setText("MapPkToTx.deploy duration:"+runtime+"\n"+message);

                                    /*writeToInternalStorage("----------DeployContract---------");
                                    writeToInternalStorage("MapPkToTx.deploy duration:"+runtime+"\n"+message);
                                    writeToInternalStorage("\n");*/
                                } catch (JSONException e) {
                                    Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                                }
                                break;
                            case "http://110.41.188.6:8080/pkDerive":
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String runtime = jsonObject.getString("runtime");
                                    String chain = jsonObject.getString("chain");
                                    String publicKey = jsonObject.getString("publicKey");
                                    item.setRegpublicKey(publicKey);
                                    // 输出或者使用这些数据
                                    mshowres.setText("SM2_PublicKeyDerive duration:"+runtime+"\npublicKey="+publicKey+"\nchain="+chain);
                                   /* writeToInternalStorage("-------SM2_PublicKeyDerive-------");
                                    writeToInternalStorage("SM2_PublicKeyDerive duration:"+runtime+"\npublicKey="+publicKey+"\nchain="+chain);
                                    writeToInternalStorage("\n");*/
                                } catch (JSONException e) {
                                    Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                                }
                                break;
                            case "http://110.41.188.6:8080/registerPk":
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String runtime = jsonObject.getString("runtime");
                                    String chain = jsonObject.getString("chain");
                                    String message = "“成功注册公钥:生成公钥证书,将公钥证书嵌入到交易发布到区块链上,更新合约内容”";
                                    String publicKey = jsonObject.getString("publicKey");

                                    //修改item的值
                                    item.setPublickey(publicKey);
                                    item.setChain(chain);

                                    // 设置文本
                                    holder.textView2.setText(item.getpublickey());
                                    holder.textView3.setText(item.getchain());

                                    String transaction = jsonObject.getString("transaction");
                                    mshowres.setText("Register_PublicKey duration:"+runtime+"\nmessage="+message+"\npublicKey="+publicKey+"\ntransaction="+transaction);
                                    /*writeToInternalStorage("-------Register_PublicKey-------");
                                    writeToInternalStorage("Register_PublicKey duration:"+runtime+"\nmessage="+message+"\npublicKey="+publicKey+"\ntransaction="+transaction);
                                    writeToInternalStorage("\n");*/
                                } catch (JSONException e) {
                                    Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                                }
                                break;
                            case "http://110.41.188.6:8080/BatchRegisterPk":
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String runtime = jsonObject.getString("runtime");
                                    String message = "“成功派生密钥五次，并注册公钥”";
                                    String publicKey = jsonObject.getString("publicKey");
                                    String transaction = jsonObject.getString("transaction");
                                    item.setPublickey(publicKey);
                                    int newKeyIndex=item.getkeyIndex()+5;
                                    item.setKeyIndex(newKeyIndex);
                                    mshowres.setText("BatchRegisterPk duration:"+runtime+"\nmessage="+message+"\npublicKey="+publicKey+"\ntransaction="+transaction);
                                  /*  writeToInternalStorage("----------BatchRegisterPk--------");
                                    writeToInternalStorage("BatchRegisterPk duration:"+runtime+"\nmessage="+message+"\npublicKey="+publicKey+"\ntransaction="+transaction);
                                    writeToInternalStorage("\n");*/
                                } catch (JSONException e) {
                                    Log.e("JSON解析错误", "解析失败: " + e.getMessage());
                                }
                                break;
                            case "http://110.41.188.6:8080/revokePk":
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String runtime = jsonObject.getString("runtime");
                                    String message = jsonObject.getString("message");

                                    mshowres.setText("Revoke_PublicKey duration:"+runtime+"\n撤销是否成功:"+message+"\n被撤销的publickey="+item.getRegpublicKey());
                                   /*  writeToInternalStorage("----------Revoke_PublicKey-------");
                                    writeToInternalStorage("Revoke_PublicKey duration:"+runtime+"\n撤销是否成功:"+message+"\n被撤销的publickey="+RegpublicKey);
                                    writeToInternalStorage("\n");*/
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
                    case "http://110.41.188.6:8080/deployContract":
                        jsonObject=JsonPut.PutJson(jsonObject,"token","0x4c26aecee34487d29adff978fd6791578ed8fd28");
                        break;
                    case "http://110.41.188.6:8080/pkDerive"://给地址返回
                        jsonObject=JsonPut.PutJson(jsonObject,"token","0x4c26aecee34487d29adff978fd6791578ed8fd28");
                        jsonObject=JsonPut.PutJson(jsonObject,"address",item.getaddress());
                        break;
                    case "http://110.41.188.6:8080/registerPk":
                        jsonObject=JsonPut.PutJson(jsonObject,"token","0x4c26aecee34487d29adff978fd6791578ed8fd28");
                        jsonObject=JsonPut.PutJson(jsonObject,"address",item.getaddress());
                        break;
                    case "http://110.41.188.6:8080/BatchRegisterPk":
                        jsonObject=JsonPut.PutJson(jsonObject,"token","0x4c26aecee34487d29adff978fd6791578ed8fd28");
                        jsonObject=JsonPut.PutJson(jsonObject,"address",item.getaddress());
                        break;
                    case "http://110.41.188.6:8080/revokePk":
                        jsonObject=JsonPut.PutJson(jsonObject,"token","0x4c26aecee34487d29adff978fd6791578ed8fd28");
                        jsonObject=JsonPut.PutJson(jsonObject,"key",item.getRegpublicKey());
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

}
