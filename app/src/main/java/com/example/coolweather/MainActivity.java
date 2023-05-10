package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ABC", "onCreateView: sta");
        setContentView(R.layout.activity_main);

        //先从sharedPreferences读取数据，不为null则直接跳转到WeatherActivity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("weather",null)!=null){
            Intent intent =new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }

    }
}