package vn.edu.ueh.ngocha.squiditytempprj.Model.dto;

import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;

public class MangaAttributes {

    private Map<String, String> title;
    private List<Map<String, String>> altTitles;
    private Map<String, String> description;

    private String originalLanguage;
    private String lastVolume;
    private String lastChapter;
    private String publicationDemographic;
    private String status;
    private Integer year;
    private String contentRating;

    private List<Tag> tags;

    private List<String> availableTranslatedLanguages;

    public Map<String, String> getTitle() {
        return title;
    }

    public List<Map<String, String>> getAltTitles() {
        return altTitles;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public String getLastVolume() {
        return lastVolume;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public String getPublicationDemographic() {
        return publicationDemographic;
    }

    public String getStatus() {
        return status;
    }

    public Integer getYear() {
        return year;
    }

    public String getContentRating() {
        return contentRating;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<String> getAvailableTranslatedLanguages() {
        return availableTranslatedLanguages;
    }
}
