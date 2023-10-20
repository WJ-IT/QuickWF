package com.example.quickwf.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.example.quickwf.data.*

interface WoojeonApiService {

    //검색화면: 라이더 검색
    @FormUrlEncoded
    @POST("WJWFRiderList.asp")
    fun selectRiderList(
        @Field("noRider") callType: String = ""
    ): Call<DBRiderList>

    @FormUrlEncoded
    @POST("WJQuicklist.asp")
    fun selectQuickList(
        @Field("Loc") callType: String = ""
    ): Call<DBDeliveryList>

    @FormUrlEncoded
    @POST("WJWFQuickStart.asp")
    fun updateQuickStart(
        @Field("NoReq") noReq: String = "",
        @Field("NoRider") noRider: String = "",
        @Field("HPRider") hpRider: String = "",
        @Field("FgQuick") fgQuick: String = "",
        @Field("CCode") cCode: String = "",
    ): Call<DBDeliveryStart>

    @FormUrlEncoded
    @POST
    fun updateQuickStartMSG(
            @Url url: String,
            @Field("NoReq") noReq: String = "",
            @Field("HPRider") hpRider: String = ""
    ): Call<DBDeliveryStart>

    @FormUrlEncoded
    @POST("WJWFRiderRegi.asp")
    fun insertRiderInfo(
        @Field("NoRider") noRider: String = "",
        @Field("NmRider") nmRider: String = "",
        @Field("HpRider") hpRider: String = "",
        @Field("methRider") methRider: String = "",
    ): Call<DBDeliveryStart>

    @FormUrlEncoded
    @POST("WJWFQuickStartInput.asp")
    fun insertDeliveryInput(
        @Field("NoRider") noRider: String = "",
        @Field("NmRider") nmRider: String = "",
        @Field("HpRider") hpRider: String = "",
        @Field("NmHosp") nmHosp: String = "",
        @Field("methRider") methRider: String = "",
    ): Call<DBDeliveryStart>

//    @FormUrlEncoded
//    @POST("WJQuickInfo.asp")
//    fun selectQuickInfo(
//            @Field("gbn") gbn: String = "",
//            @Field("value") value: String = ""
//    ): Call<DBDeliveryInfo>
//
//    @FormUrlEncoded
//    @POST("WJQuickCancel.asp")
//    fun cancelQuick(
//            @Field("NoReq") noReq: String = ""
//    ): Call<DBDeliveryStart>

    companion object {
        fun create():WoojeonApiService {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                   // .addHeader("X-Naver-Client-Id", CLIENT_ID)
                    //.addHeader("X-Naver-Client-Secret", CLIENT_SECRET)
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://iclkorea.com:23003/android/quick/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WoojeonApiService::class.java)
        }
    }
}