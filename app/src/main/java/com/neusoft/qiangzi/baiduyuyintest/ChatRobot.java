package com.neusoft.qiangzi.baiduyuyintest;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ChatRobot {
    private static final String TAG = "ChatRobot";
    private final static String QINGYUNKE_URL = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=";
    private final static String TURING_URL = "http://openapi.tuling123.com/openapi/api/v2";
    private final static String BAIDU_URL = "https://www.baidu.com";
    private Context context;
    private RequestQueue queue;
    private OnResponseListener responseListener;

    public ChatRobot(Context context) {
        this.context = context;
        queue = Volley.newRequestQueue(context);
    }

    public void speakToQingyun(String text){
        String str;
        try {
            str = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        String url = QINGYUNKE_URL + str;
        Log.d(TAG, "question: url="+url);

        StringRequest request = new StringRequest(
                StringRequest.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "onResponse: response="+response);

                        Gson gson = new Gson();
                        QingyunResult r = gson.fromJson(response, QingyunResult.class);
                        if(r!=null){
                            responseListener.OnResponse(r.content);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: ", error);
                    }
                }
        );
        queue.add(request);
    }

    public void speakToTuring(String text){
        String str;
        try {
            str = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        String url = QINGYUNKE_URL + str;
        Log.d(TAG, "question: url="+url);

        StringRequest request = new StringRequest(
                StringRequest.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "onResponse: response="+response);

                        Gson gson = new Gson();
                        QingyunResult r = gson.fromJson(response, QingyunResult.class);
                        if(r!=null){
                            responseListener.OnResponse(r.content);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: ", error);
                    }
                }
        );
        queue.add(request);
    }

    public void setOnResponseListener(OnResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    public interface OnResponseListener{
        void OnResponse(String response);
    }

    static class QingyunResult {
        int result;
        String content;
    }
}
