package com.kadircolak.parket;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JsonWebApi {
    @GET("ispark/Park")
    Call<List<Post>> getData();
}
