package com.robotemplates.webviewapp.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.robotemplates.webviewapp.Model.DataRadius;
import com.robotemplates.webviewapp.Model.DataWifi;
import com.robotemplates.webviewapp.Model.ResponseModel;
import com.robotemplates.webviewapp.Network.ApiConfig;
import com.robotemplates.webviewapp.Network.IAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Manda on 12/09/2017.
 */


public class webappinterface {
    Context mContext;
    private List<ScanResult> results = new ArrayList<>();
    private Retrofit retrofit;

    /** Instantiate the interface and set the context */
    public webappinterface(Context c) {
        mContext = c;
        initRetrofit();
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, "tes", Toast.LENGTH_SHORT).show();
        scanWifi();
    }

    	private void scanWifi(){
		@SuppressLint("WifiManagerLeak") final
		WifiManager wifi = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);

            wifi.startScan();
            results = wifi.getScanResults();
            Collections.sort(results, new ScanResultComparator());
//            adapter.setData(buatModelWifi(results));
//            adapter.notifyDataSetChanged();

            List<DataWifi> listData = new ArrayList<>();
            listData = buatModelWifi(results);

            if(listData.size() > 2){
                // pakai android.
                Log.d("LOG_RESPONSE", "masuk");
                kirimDataRadius(String.valueOf(listData.get(0).getJarak()), String.valueOf(listData.get(1).getJarak())
                    , String.valueOf(listData.get(2).getJarak()));
            }else{
                // pakai yang web.
            }
//
//            Log.d("LOG_RESULT", wifi.getScanResults().toString());
//            Log.d("LOG_RESULT_2", buatModelWifi(results).toString());
	}


    private List<DataWifi> buatModelWifi(List<ScanResult> results){
        List<DataWifi> listData = new ArrayList<>();

        for(ScanResult dataScan : results){
            DataWifi tmpData = new DataWifi(dataScan.SSID, dataScan.frequency, dataScan.level);

            // if untuk filter jarak disini
            double distance = DistanceUtil.calculateDistance((double) tmpData.getLevel(),
                    (double) tmpData.getFrekuensi());
            tmpData.setJarak(distance);

            if(distance < 10) {
                listData.add(tmpData);
            }
        }

        return listData;
    }

    private void kirimDataRadius(String dataRadius1, String dataRadius2, String dataRadius3){
        IAPI iApi = retrofit.create(IAPI.class);
        Call<DataRadius> data = iApi.postDataRadius(dataRadius1, dataRadius2, dataRadius3);
        data.enqueue(new Callback<DataRadius>() {
            @Override
            public void onResponse(Call<DataRadius> call, Response<DataRadius> response) {
                Log.d("LOG_RESPONSE", response.toString());
                Log.d("LOG_RESPONSE_2", response.body().getPesan());
            }

            @Override
            public void onFailure(Call<DataRadius> call, Throwable t) {
                t.printStackTrace();
                Log.d("LOG_FAIL", "fail");

            }
        });
    } //kirim data radius

    @JavascriptInterface
    public void kirimDataHazard(String dataCookieshazard,
                                 String dataIdUser,
                                 String dataTypehazard,
                                 String dataPenyebabhazard,
                                 String dataDeskripsi,
                                 String dataKodeBandara,
                                 String dataImage,
                                 String dataImage1,
                                 String dataLatitude,
                                 String dataLongitude,
                                 String dataWaktu){

        Log.d("LOG_HAZARD", dataTypehazard);

        IAPI iApi = retrofit.create(IAPI.class);
        Call<ResponseModel> data = iApi.postDataHazard(dataCookieshazard, dataIdUser, dataTypehazard, dataPenyebabhazard,
                dataDeskripsi,  dataKodeBandara, "a", "b", dataLatitude, dataLongitude, dataWaktu);
        data.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                Log.d("LOG_RESPONSE", response.toString());
                Log.d("LOG_RESPONSE_2", response.body().getPesan());
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                t.printStackTrace();
                Log.d("LOG_FAIL", "fail");

            }
        });
    } //kirim data hazard

    private void kirimDataEvent(String dataImage,
                                 String dataImage1,
                                 String dataIdUser,
                                 String dataCookiesevent,
                                 String dataKerugian,
                                 String dataKodeBandara,
                                 String dataLatitude,
                                 String dataLongitude){
        IAPI iApi = retrofit.create(IAPI.class);
        Call<ResponseModel> data = iApi.postDataEvent(dataImage, dataImage1, dataIdUser, dataCookiesevent, dataKerugian,
                dataKodeBandara, dataLatitude, dataLongitude);
        data.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                Log.d("LOG_RESPONSE", response.toString());
//                Log.d("LOG_RESPONSE_2", response.body().getPesan());
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                t.printStackTrace();
                Log.d("LOG_FAIL", "fail");

            }
        });
    } //kirim data event

    private void initRetrofit(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        OkHttpClient client = new OkHttpClient();

        retrofit = new Retrofit.Builder().baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }
}
