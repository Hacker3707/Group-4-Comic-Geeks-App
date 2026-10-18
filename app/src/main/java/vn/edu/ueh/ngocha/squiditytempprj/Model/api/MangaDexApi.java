package vn.edu.ueh.ngocha.squiditytempprj.Model.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaSingleResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaTagResponse;

public interface MangaDexApi {

    @GET("manga")
    Call<MangaResponse> searchManga(
            @Query("title") String title,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("includes[]") String include
    );

    @GET("manga/{id}")
    Call<MangaSingleResponse> getMangaById(
            @Path("id") String mangaId,
            @Query("includes[]") List<String> include
    );

    @GET("manga/tag")
    Call<MangaTagResponse> getMangaTags();

    @GET("manga")
    Call<MangaResponse> getMangaByGenre(
            @Query("includedTags[]") String tagId,
            @Query("limit") int limit,
            @Query("includes[]") List<String> includes
    );

}