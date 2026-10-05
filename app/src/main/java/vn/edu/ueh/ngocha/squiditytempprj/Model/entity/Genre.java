package vn.edu.ueh.ngocha.squiditytempprj.Model.entity;

public class Genre {
    private String genreId;
    private String genreName;

    public Genre (String genreId, String genreName) {
        this.genreId = genreId;
        this.genreName = genreName;
    }

    // =================================
    // Getter methods for each field
    // =================================

    public String getGenreId() {
        return genreId;
    }

    public String getGenreName() {
        return genreName;
    }

    // =================================
    // Setter methods for each field
    // =================================

    public void setGenreId(String genreId) {
        this.genreId = genreId;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }
}
