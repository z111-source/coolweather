package com.example.coolweather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private Button btn_back;
    private TextView textTitle;
    private ListView listItem;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private List<String> dataList=new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int currentLevel;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_choose_area,container,false);
        btn_back = view.findViewById(R.id.button_back);
        textTitle =  view.findViewById(R.id.text_title);
        listItem =  view.findViewById(R.id.list_item);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listItem.setAdapter(adapter);
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //listview的点击选项监听
        listItem.setOnItemClickListener((parent, view, position, id) -> {
            if(currentLevel==LEVEL_PROVINCE){
                selectedProvince=provinceList.get(position);
                queryCities();
            }else if(currentLevel==LEVEL_CITY){
                selectedCity=cityList.get(position);
                queryCounties();
            }
        });

        //返回按钮监听    省列表界面不用返回
        btn_back.setOnClickListener(v -> {
            if(currentLevel==LEVEL_COUNTY){
                queryCities();
            }else if(currentLevel==LEVEL_CITY){
                queryProvince();
            }
        });
        queryProvince();
    }

    //遍历省界面
    private void queryProvince() {
        btn_back.setVisibility(View.GONE);
        textTitle.setText("中国");
        provinceList= LitePal.findAll(Province.class);  //LitePal.findAll();
        if(provinceList.size()>0){
            dataList.clear();       //注意要清屏

            for(Province province:provinceList){            //遍历provinceList 并添加到dataList
                dataList.add(province.getProvinceName());   //dataList加载显示，当前列表名字，相当于媒介
            }
            adapter.notifyDataSetChanged();     //通知数据集更新
            currentLevel=LEVEL_PROVINCE;
            listItem.setSelection(0);   //第一项置顶
        } else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");         //数据库找不到，去服务器查询
        }
    }
    private void queryCities() {
        textTitle.setText(selectedProvince.getProvinceName());
        btn_back.setVisibility(View.VISIBLE);
        cityList= LitePal.where("provinceId = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            currentLevel=LEVEL_CITY;
            listItem.setSelection(0);
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    private void queryCounties() {
        textTitle.setText(selectedCity.getCityName());
        btn_back.setVisibility(View.VISIBLE);
        countyList=LitePal.where("cityId = ?",
                String.valueOf(selectedCity.getId())).find(County.class);       //String.valueOf()
        if(countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            currentLevel=LEVEL_COUNTY;
            listItem.setSelection(0);
        }else {
            int cityCode=selectedCity.getCityCode();
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china"+provinceCode+"/"+cityCode;    // 注意加"/"
            queryFromServer(address,"county");
        }
    }
    private void queryFromServer(String address,final String type){

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;               //result先设为false
                //解析响应，返回result为 true解析成功
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                } else if("city".equals(type)) {
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }

                //请求响应解析成功，返回主线程显示页面
                if(result){
                    getActivity().runOnUiThread(() -> {
                        //关闭提示

                        if("province".equals(type)){
                            queryProvince();
                        }else if ("city".equals(type)){
                            queryCities();
                        }else if ("county".equals(type)){
                            queryCounties();
                        }
                    });
                }
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //转入活动主线程
                //getActivity()碎片转入活动
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(),
                        "加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }


}