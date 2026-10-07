package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

public class Relationship {

    private String id;
    private String type;
    private String related;
    private CoverAttributes attributes;


    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getRelated() {
        return related;
    }

    public CoverAttributes getAttributes() {
        return attributes;
    }
}
