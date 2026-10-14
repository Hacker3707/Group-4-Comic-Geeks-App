package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

import com.google.gson.annotations.SerializedName;

public class AuthorAttributes {
    @SerializedName("name")
    private String author_name;

    public String getAuthorName() {
        return author_name;
    }

}
