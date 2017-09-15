package com.robotemplates.webviewapp.Network;

import com.google.android.gms.wearable.DataEvent;
import com.robotemplates.webviewapp.Model.DataRadius;
import com.robotemplates.webviewapp.Model.DataWifi;
import com.robotemplates.webviewapp.Model.ResponseModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by Manda on 11/08/2017.
 */

public interface IAPI {
    @GET(ApiConfig.GET_URL)
    Call<DataWifi> getData(@Path("sort") String order,
                           @Query("api_key") String apiKey);

//    @FormUrlEncoded
//    @POST(ApiConfig.POST_URL)
//    Call<DataWifi> postData(@Field("radius") String deviceID);

    @FormUrlEncoded
    @POST(ApiConfig.POST_URL)
    Call<DataRadius> postDataRadius(@Field("radius1") String deviceRadius1, @Field("radius2") String
            deviceRadius2, @Field("radius3") String deviceRadius3);



    @FormUrlEncoded
    @POST(ApiConfig.POST_URL_HAZARD)
    Call<ResponseModel> postDataHazard(@Field("cookieshazard") String deviceCookieshazard,
                                       @Field("id_user") String deviceIdUser,
                                       @Field("typehazard") String deviceTypehazard,
                                       @Field("penyebabhazard") String devicePenyebabhazard,
                                       @Field("deskripsi") String deviceDeskripsi,
                                       @Field("kode_bandara") String deviceKodeBandara,
                                       @Field("image") String deviceImage,
                                       @Field("image1") String deviceImage1,
                                       @Field("latitude") String deviceLatitude,
                                       @Field("longitude") String deviceLongitude,
                                       @Field("waktu") String deviceWaktu); //diganti nama db

    @FormUrlEncoded
    @POST(ApiConfig.POST_URL_EVENT)
    Call<ResponseModel> postDataEvent(@Field("image") String deviceImage,
                                  @Field("image1") String deviceImage1,
                                  @Field("id_user") String deviceIdUser,
                                  @Field("cookiesevent") String deviceCookiesevent,
                                  @Field("kerugian") String deviceKerugian,
                                  @Field("kode_bandara") String deviceKodeBandara,
                                  @Field("latitude") String deviceLatitude,
                                  @Field("longitude") String deviceLongitude); //diganti nama db
}
