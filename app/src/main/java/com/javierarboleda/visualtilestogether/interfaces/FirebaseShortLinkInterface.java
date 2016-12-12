package com.javierarboleda.visualtilestogether.interfaces;

import com.javierarboleda.visualtilestogether.models.ShortLinkRequest;
import com.javierarboleda.visualtilestogether.models.ShortLinkResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit interface for requesting short links from Firebase.
 * Created by chris on 12/11/16.
 */
public interface FirebaseShortLinkInterface {
    @POST("v1/shortLinks")
    Call<ShortLinkResponse> buildShortLink(@Body ShortLinkRequest request,
                                           @Query("key") String api_key);
}
