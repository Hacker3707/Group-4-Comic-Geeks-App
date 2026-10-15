package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class TagAttributes {

    @SerializedName("name")
    private Map<String, String> name;

    @SerializedName("description")
    private Map<String, String> description;

    @SerializedName("group")
    private String group;

    @SerializedName("version")
    private int version;

    public Map<String, String> getName() {
        return name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public String getGroup() {
        return group;
    }

    public int getVersion() {
        return version;
    }
}