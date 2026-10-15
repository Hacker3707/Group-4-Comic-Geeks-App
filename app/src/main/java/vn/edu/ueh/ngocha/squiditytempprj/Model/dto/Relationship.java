package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

import com.google.gson.annotations.SerializedName;

public class Relationship {

    private String id;
    private String type;
    private String related;

    @SerializedName("attributes")
    private RelationshipAttributes attributes;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getRelated() {
        return related;
    }

    public RelationshipAttributes getAttributes() {
        return attributes;
    }
}