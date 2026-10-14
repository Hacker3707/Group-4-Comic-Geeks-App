package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import vn.edu.ueh.ngocha.squiditytempprj.Model.api.MangaDexApi;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaResponse;

public class MangaDexRepository {
    private final MangaDexApi mangaDexApi;

    public MangaDexRepository(MangaDexApi mangaDexApi) {
        this.mangaDexApi = mangaDexApi;
    }



}
