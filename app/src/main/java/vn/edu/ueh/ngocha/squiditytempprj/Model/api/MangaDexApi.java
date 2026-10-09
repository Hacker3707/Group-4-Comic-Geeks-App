package vn.edu.ueh.ngocha.squiditytempprj.Model.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;

public interface MangaDexApi {

    @GET("manga")
    Call<MangaResponse> searchManga(
            @Query("title") String title,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("includes[]") String include
    );
}