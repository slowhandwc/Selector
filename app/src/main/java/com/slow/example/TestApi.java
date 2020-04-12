package com.slow.example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * 获取地址信息
 * @Author wuchao
 * @Date 2020/4/12-12:54 PM
 * @description
 * @email 329187218@qq.com
 * @see
 */
public interface TestApi {
    @Headers("Authorization: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImp0aSI6IjVlOTJiYTQ4NjUwNDQifQ.eyJpc3MiOiJodHRwOlwvXC95Y2dqLmpjLWRldi5jbiIsImp0aSI6IjVlOTJiYTQ4NjUwNDQiLCJpYXQiOjE1ODY2NzQyNDgsImV4cCI6MTU4OTI2NjI0OCwidXNlckluZm8iOnsiaWQiOjU2NiwiYWNjb3VudCI6IiIsInBob25lIjoiMTM0Mjk4OTQwNTMiLCJyZWFsbmFtZSI6IiIsImNhcmRfbm8iOiIiLCJ1c2VybmFtZSI6IjEzNDI5ODk0MDUzIiwic3RhdHVzIjoxLCJyb2xlX2lkIjo5LCJyb2xlX25hbWUiOiJcdTRmNTNcdTlhOGNcdThkMjZcdTYyMzciLCJncm91cF9pZCI6NCwiZ3JvdXBfbGV2ZWwiOjQsImRpc3RyaWN0IjpbeyJwcm92aW5jZV9pZCI6MjYsImNpdHlfaWQiOjMzNiwiYXJlYV9pZCI6MjgzNywic3RyZWV0X2lkIjowfV19fQ.R-3Tur8_wCDVe4l88vF4q7i4cIm-2Lr93xI1GyKN3TY")
    @GET("app/v1/district/getList")
    Call<AddressResult> getAddress(@Query("action") String action, @Query("id") String id);
}
