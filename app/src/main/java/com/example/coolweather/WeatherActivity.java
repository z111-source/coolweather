package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.service.ForeService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_updateTime);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        //加入下拉刷新
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(   //刷新图标颜色
                androidx.navigation.ui.R.color.design_default_color_primary);

        //加入滑动菜单
        Button menu = findViewById(R.id.btn_menu);
        drawerLayout = findViewById(R.id.drawer);
        menu.setOnClickListener(this);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                this);
        String weatherString = prefs.getString("weather", null);//null ?
        final String weatherId;
        if (weatherString != null) {
            //有数据直接解析显示
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //没数据则发起请求
            weatherId = getIntent().getStringExtra("weather_id");
            //“weather_id”是intent从fragment县列表的传来的county的weatherId

            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);      //请求数据
        }
        swipeRefresh.setOnRefreshListener(() -> requestWeather(weatherId));

    }


    public void requestWeather(final String weatherId) {
        //why public,final
        String weatherUrl = "http://guolin.tech/api/weather?cityid="
                + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "加载失败",
                            Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //responseText为收取到的json数据
                final String responseText = response.body().string();
                //weather为GSON解析完成的Weather类
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(() -> {
                    if (weather != null && "ok".equals(weather.status)) {
                        //SharedPreferences.Editor
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(WeatherActivity.this).edit();

                        //保存weather的json数据
                        editor.putString("weather", responseText);
                        editor.apply();

                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(WeatherActivity.this, "获取天气失败",
                                Toast.LENGTH_SHORT).show();
                    }
                    swipeRefresh.setRefreshing(false);
                });
            }
        });

    }

    //显示天气信息页面
    private void showWeatherInfo(Weather weather) {
        if (weather != null && "ok".equals(weather.status)) {
            String cityName = weather.basic.cityName;
            String updateTime = weather.basic.update.updateTime.split(" ")[1];
            //.spilt(" ")以空格为分隔符    【1】？
            String degree = weather.now.temperature + "℃";
            String weatherInfo = weather.now.more.info;
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();

            for (Forecast forecast : weather.forecastList) {
                //.inflate() forecastLayout是forecast_item父布局
                View view = LayoutInflater.from(this).inflate
                        (R.layout.forecast_item, forecastLayout, false);

                TextView dateText = view.findViewById(R.id.date_text);
                TextView infoText = view.findViewById(R.id.info_text);
                TextView maxText = view.findViewById(R.id.max_text);
                TextView minText = view.findViewById(R.id.min_text);
                dateText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max);
                minText.setText(forecast.temperature.min);

                forecastLayout.addView(view);
            }
            if (weather.aqi != null) {
                // !=null   ?
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }
            String comfort = "舒适度：" + weather.suggestion.comfort.info;
            String carWash = "洗车指数：" + weather.suggestion.carWash.info;
            String sport = "运动建议：" + weather.suggestion.sport.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);

            weatherLayout.setVisibility(View.VISIBLE);
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        } else {
            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    protected void onDestroy() {
        Intent i = new Intent(this, ForeService.class);
        stopService(i);
        super.onDestroy();
    }
}
