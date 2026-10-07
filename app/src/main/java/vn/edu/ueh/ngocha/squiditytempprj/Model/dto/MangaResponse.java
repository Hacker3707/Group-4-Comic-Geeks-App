package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

import java.util.List;

public class MangaResponse {

    private String result;
    private String response;
    private List<MangaData> data;
    private int limit;
    private int offset;
    private int total;

    public String getResult() {
        return result;
    }

    public String getResponse() {
        return response;
    }

    public List<MangaData> getData() {
        return data;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getTotal() {
        return total;
    }
}
