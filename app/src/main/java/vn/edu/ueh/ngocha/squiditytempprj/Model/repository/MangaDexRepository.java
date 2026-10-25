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
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;

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

    public LiveData<List<Genre>> getGenresLive() {
        MutableLiveData<List<Genre>> genresLiveData =
                new MutableLiveData<>();

        api.getMangaTags().enqueue(
                new Callback<MangaTagResponse>() {
                    @Override
                    public void onResponse(
                            Call<MangaTagResponse> call,
                            Response<MangaTagResponse> response) {

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().getData() == null) {
                            Log.e("GenreAPI",
                                    "Cannot load genres. HTTP "
                                            + response.code());
                            return;
                        }

                        List<Genre> genres = new ArrayList<>();

                        for (Tag tag : response.body().getData()) {
                            if (tag.getAttributes() == null) {
                                continue;
                            }

                            if (!"genre".equals(
                                    tag.getAttributes().getGroup())) {
                                continue;
                            }

                            Map<String, String> names =
                                    tag.getAttributes().getName();

                            if (names == null) {
                                continue;
                            }

                            String name = names.get("en");

                            if (name == null || name.trim().isEmpty()) {
                                continue;
                            }

                            // Chuyển DTO Tag thành Room entity Genre
                            Genre genre = new Genre(
                                    tag.getId(),
                                    name
                            );

                            genres.add(genre);
                        }

                        genresLiveData.setValue(genres);

                        Log.d("GenreAPI",
                                "Number of genres: " + genres.size());
                    }

                    @Override
                    public void onFailure(
                            Call<MangaTagResponse> call,
                            Throwable t) {
                        Log.e("GenreAPI",
                                "Failed to load genres", t);
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

    private final MutableLiveData<List<MangaData>> searchResults =
            new MutableLiveData<>();

    private Call<MangaResponse> currentSearchCall;
    private int searchRequestVersion = 0;

    public LiveData<List<MangaData>> getSearchResults() {
        return searchResults;
    }

    public synchronized void searchManga(
            String query,
            int limit,
            int offset,
            String include
    ) {
        final int requestVersion = ++searchRequestVersion;

        // Hủy request trước đó nếu vẫn còn đang chạy.
        if (currentSearchCall != null) {
            currentSearchCall.cancel();
            currentSearchCall = null;
        }

        if (query == null || query.trim().isEmpty()) {
            searchResults.setValue(new ArrayList<>());
            return;
        }

        final String requestedQuery = query.trim();

        Call<MangaResponse> call = api.searchManga(
                requestedQuery,
                limit,
                offset,
                include
        );

        currentSearchCall = call;

        call.enqueue(new Callback<MangaResponse>() {
            @Override
            public void onResponse(
                    Call<MangaResponse> call,
                    Response<MangaResponse> response
            ) {
                synchronized (MangaDexRepository.this) {
                    // Bỏ qua kết quả của request cũ.
                    if (requestVersion != searchRequestVersion) {
                        return;
                    }

                    if (currentSearchCall == call) {
                        currentSearchCall = null;
                    }
                }

                if (!response.isSuccessful()
                        || response.body() == null
                        || response.body().getData() == null) {
                    Log.e("MangaSearch",
                            "Search failed. HTTP " + response.code());
                    return;
                }

                searchResults.postValue(response.body().getData());
            }

            @Override
            public void onFailure(
                    Call<MangaResponse> call,
                    Throwable t
            ) {
                synchronized (MangaDexRepository.this) {
                    if (requestVersion != searchRequestVersion) {
                        return;
                    }

                    if (currentSearchCall == call) {
                        currentSearchCall = null;
                    }
                }

                if (!call.isCanceled()) {
                    Log.e("MangaSearch", "Search request failed", t);
                }
            }
        });
    }


    public LiveData<List<MangaData>> getMangaByIdList(List<String> mangaIds) {
        MutableLiveData<List<MangaData>> result =
                new MutableLiveData<>();

        if (mangaIds == null || mangaIds.isEmpty()) {
            result.setValue(new ArrayList<MangaData>());
            return result;
        }

        final List<MangaData> mangaList = new ArrayList<>();
        final int[] completedRequests = {0};
        final int totalRequests = mangaIds.size();

        for (String mangaId : mangaIds) {
            if (mangaId == null || mangaId.trim().isEmpty()) {
                completedRequests[0]++;
                continue;
            }

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
                                    && response.body() != null
                                    && response.body().getData() != null) {
                                synchronized (mangaList) {
                                    mangaList.add(response.body().getData());
                                }
                            } else {
                                Log.e("MangaDexRepository",
                                        "Failed to get manga: " + mangaId);
                            }

                            synchronized (completedRequests) {
                                completedRequests[0]++;

                                if (completedRequests[0] == totalRequests) {
                                    result.postValue(
                                            new ArrayList<MangaData>(mangaList)
                                    );
                                }
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<MangaSingleResponse> call,
                                Throwable t) {

                            Log.e("MangaDexRepository",
                                    "Request failed for " + mangaId, t);

                            synchronized (completedRequests) {
                                completedRequests[0]++;

                                if (completedRequests[0] == totalRequests) {
                                    result.postValue(
                                            new ArrayList<MangaData>(mangaList)
                                    );
                                }
                            }
                        }
                    });
        }

        return result;
    }


}
