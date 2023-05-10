package com.example.networktest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_request).setOnClickListener(this);
        text = findViewById(R.id.response_text);

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_request){
            sendRequestWithOkhttp();
        }
    }

    private void sendRequestWithOkhttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://10.0.2.2/get_data.json")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseJSONWithJSONObject(responseData);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private void parseJSONWithJSONObject(String jsonData) {
                try {
                    JSONArray jsonArray=new JSONArray(jsonData);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject=jsonArray.getJSONObject(i);
                        String id=jsonObject.getString("id");
                        String name=jsonObject.getString("name");
                        Log.d("mainActivity", "id="+id);
                        Log.d("mainActivity", "name="+name);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

            private void showResponse(String responseData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text.setText(responseData);
                    }
                });
    }
}