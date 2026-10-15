package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

import com.google.gson.annotations.SerializedName;

public class RelationshipAttributes {

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("name")
    private String name;

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }
}