package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Tag {

    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type;

    @SerializedName("attributes")
    private TagAttributes attributes;

    @SerializedName("relationships")
    private List<Relationship> relationships;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public TagAttributes getAttributes() {
        return attributes;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }
}