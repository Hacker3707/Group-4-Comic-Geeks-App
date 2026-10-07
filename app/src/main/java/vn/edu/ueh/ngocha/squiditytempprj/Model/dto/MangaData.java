package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

import java.util.List;

public class MangaData {

    private String id;
    private String type;
    private MangaAttributes attributes;
    private List<Relationship> relationships;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public MangaAttributes getAttributes() {
        return attributes;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }
}