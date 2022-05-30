package com.kadircolak.parket;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface PrivateStations {
    @GET("proje/otopark/home/ozelotopark")
    Call<List<Post3>> getLocation();
}
