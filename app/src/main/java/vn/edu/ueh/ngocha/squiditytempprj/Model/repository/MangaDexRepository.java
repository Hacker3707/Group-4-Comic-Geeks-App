package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.api.RetrofitClient;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaSingleResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaTagResponse;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;

public class MangaDexRepository {
    private final MangaDexApi api;

    public MangaDexRepository() {
        api = RetrofitClient.getMangaDexApi();
    }

    private final MutableLiveData<List<MangaData>> mangaList =
            new MutableLiveData<List<MangaData>>(
                    new ArrayList<MangaData>());

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<Boolean>(false);

    private String currentQuery = "";
    private int currentOffset = 0;

    private final int PAGE_SIZE = 20;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    public LiveData<List<MangaData>> getMangaList() {
        return mangaList;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void searchComics(String query) {
        currentQuery = query;
        currentOffset = 0;
        isLastPage = false;

        mangaList.setValue(new ArrayList<MangaData>());

        loadNextPage();
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

    public LiveData<List<Tag>> getGenresLive() {
        MutableLiveData<List<Tag>> genresLiveData =
                new MutableLiveData<>();

        api.getMangaTags().enqueue(
                new Callback<MangaTagResponse>() {
                    @Override
                    public void onResponse(
                            Call<MangaTagResponse> call,
                            Response<MangaTagResponse> response) {

                        Log.d("GenreAPI", "HTTP code: " + response.code());

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {

                            List<Tag> genres =
                                    new ArrayList<>();

                            for (Tag tag :
                                    response.body().getData()) {

                                if (tag.getAttributes() == null) {
                                    continue;
                                }

                                if (!"genre".equals(
                                        tag.getAttributes().getGroup())) {
                                    continue;
                                }

                                Map<String, String> names =
                                        tag.getAttributes().getName();

                                if (names != null
                                        && names.get("en") != null) {
                                    genres.add(tag);
                                }
                            }

                            Log.d("GenreAPI",
                                    "Number of genres: " + genres.size());

                            genresLiveData.setValue(genres);

                        } else {
                            Log.e(
                                    "MangaDexRepository",
                                    "Cannot load genres. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<MangaTagResponse> call,
                            Throwable t) {

                        Log.e(
                                "MangaDexRepository",
                                "Failed to load genres",
                                t
                        );
                    }
                }
        );

        return genresLiveData;
    }

    public LiveData<List<MangaData>> getMangaByGenreLive(
            String tagId) {

        MutableLiveData<List<MangaData>> mangaLiveData =
                new MutableLiveData<>();

        List<String> includes = new ArrayList<>();
        includes.add("cover_art");

        api.getMangaByGenre(
                tagId,
                20,
                includes
        ).enqueue(
                new Callback<MangaResponse>() {
                    @Override
                    public void onResponse(
                            Call<MangaResponse> call,
                            Response<MangaResponse> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {

                            mangaLiveData.setValue(
                                    response.body().getData()
                            );

                        } else {
                            Log.e(
                                    "MangaDexRepository",
                                    "Cannot load manga by genre. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<MangaResponse> call,
                            Throwable t) {

                        Log.e(
                                "MangaDexRepository",
                                "Failed to load manga by genre",
                                t
                        );
                    }
                }
        );

        return mangaLiveData;
    }

    public LiveData<List<MangaData>> searchManga(
            String searchQuery,
            int limit,
            int offset,
            String include) {
        MutableLiveData<List<MangaData>> mangaLiveData =
                new MutableLiveData<>();

        api.searchManga(searchQuery, limit, offset, include)
                .enqueue(
                new Callback<MangaResponse>() {
                    @Override
                    public void onResponse(
                            Call<MangaResponse> call,
                            Response<MangaResponse> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getData() != null) {
                            mangaLiveData.setValue(
                                    response.body().getData()
                            );
                        } else {
                            Log.e(
                                    "MangaDexRepository",
                                    "Cannot search manga. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<MangaResponse> call,
                            Throwable t) {
                        Log.e(
                                "MangaDexRepository",
                                "Failed to search manga",
                                t
                        );
                    }


                }
        );
        return mangaLiveData;
    }

    public void loadNextPage() {
        if (isLoading || isLastPage) {
            return;
        }

        isLoading = true;
        loading.setValue(true);

        api.searchManga(
                currentQuery,
                PAGE_SIZE,
                currentOffset,
                "cover_art"
        ).enqueue(new Callback<MangaResponse>() {
            @Override
            public void onResponse(
                    Call<MangaResponse> call,
                    Response<MangaResponse> response) {

                isLoading = false;
                loading.setValue(false);

                if (!response.isSuccessful()
                        || response.body() == null) {
                    Log.e("MangaDexRepository",
                            "API error: " + response.code());
                    return;
                }

                MangaResponse result = response.body();
                List<MangaData> newItems = result.getData();

                if (newItems == null || newItems.isEmpty()) {
                    isLastPage = true;
                    return;
                }

                List<MangaData> updatedList = mangaList.getValue();

                if (updatedList == null) {
                    updatedList = new ArrayList<MangaData>();
                } else {
                    updatedList =
                            new ArrayList<MangaData>(updatedList);
                }

                updatedList.addAll(newItems);
                mangaList.setValue(updatedList);

                currentOffset += newItems.size();

                if (currentOffset >= result.getTotal()
                        || newItems.size() < PAGE_SIZE) {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(
                    Call<MangaResponse> call,
                    Throwable t) {

                isLoading = false;
                loading.setValue(false);

                Log.e("MangaDexRepository",
                        "Failed to load manga", t);
            }
        });
    }

}
