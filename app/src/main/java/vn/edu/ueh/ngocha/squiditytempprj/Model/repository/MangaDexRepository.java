package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaSingleResponse;

public class MangaDexRepository {
    private final MangaDexApi api;

    public MangaDexRepository() {
        api = RetrofitClient.getMangaDexApi();
    }

    public LiveData<MangaData> getMangaById(String mangaId) {

        MutableLiveData<MangaData> mangaLiveData =
                new MutableLiveData<>();

        List<String> includes = new ArrayList<>();
        includes.add("cover_art");
        includes.add("author");

        api.getMangaById(mangaId, includes)
                .enqueue(new Callback<MangaSingleResponse>() {

                    @Override
                    public void onResponse(
                            Call<MangaSingleResponse> call,
                            Response<MangaSingleResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            mangaLiveData.setValue(
                                    response.body().getData()
                            );

                        } else {
                            Log.e("MangaDexRepository",
                                    "API error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<MangaSingleResponse> call,
                            Throwable t) {

                        Log.e("MangaDexRepository",
                                "Request failed", t);
                    }
                });

        return mangaLiveData;
    }

}
