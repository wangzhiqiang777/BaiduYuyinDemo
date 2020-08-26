package com.neusoft.qiangzi.baiduyuyintest.ChatRobot;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

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
        Log.d(TAG, "speakToQingyun: url="+url);

        StringRequest request = new StringRequest(
                StringRequest.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d(TAG, "onResponse: response="+response);

                        Gson gson = new Gson();
                        QingyunResponse r = gson.fromJson(response, QingyunResponse.class);
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
        Log.d(TAG, "speakToTuring: text="+text);

        final TuringRequest turingRequest = new TuringRequest();
        turingRequest.setInputText(text);
        Gson gson = new Gson();
        String json = gson.toJson(turingRequest, TuringRequest.class);
        Log.d(TAG, "speakToTuring: json="+json);
        JSONObject params = null;
        try {
            params=new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "speakToTuring: ", e);
            e.printStackTrace();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(
                TURING_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, "onResponse: response="+response.toString());
                        Gson gson = new Gson();
                        TuringResponse turingResponse = gson.fromJson(response.toString(), TuringResponse.class);
                        if(turingRequest!=null){
                            for (TuringResponse.ResultsBean result:turingResponse.getResults()
                                 ) {
                                Log.d(TAG, "onResponse: result="+result.getResultType());
                                if(result.getResultType().equals("text")){
                                    Log.d(TAG, "onResponse: text="+result.getValues().getText());
                                    responseListener.OnResponse(result.getValues().getText());
                                }
                            }
                        }else {
                            Log.e(TAG, "onResponse: Gson parse error");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: ", error);
                    }
                });

        queue.add(request);
    }

    public void setOnResponseListener(OnResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    public interface OnResponseListener{
        void OnResponse(String response);
    }
}
