package vn.edu.ueh.ngocha.squiditytempprj.Model.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaAttributes;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Tag;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;

public final class ComicMapper {
    private ComicMapper() {}

    public static Comic toEntity(MangaData manga) {
        if (manga == null || manga.getId() == null || manga.getId().trim().isEmpty()) {
            return null;
        }

        MangaAttributes a = manga.getAttributes();
        String title = null, description = null, author = null, coverUrl = null;
        String status = null, genres = null, rating = null, language = null;
        Integer year = null;
        String lastVolume = null, lastChapter = null;

        if (a != null) {
            title = localized(a.getTitle());
            description = localized(a.getDescription());
            status = a.getStatus();
            rating = a.getContentRating();
            language = a.getOriginalLanguage();
            year = a.getYear();
            lastVolume = a.getLastVolume();
            lastChapter = a.getLastChapter();

            List<String> names = new ArrayList<>();
            if (a.getTags() != null) {
                for (Tag tag : a.getTags()) {
                    if (tag == null || tag.getAttributes() == null
                            || !"genre".equals(tag.getAttributes().getGroup())) continue;
                    String name = localized(tag.getAttributes().getName());
                    if (name != null && !name.trim().isEmpty()) names.add(name);
                }
            }
            genres = names.isEmpty() ? null : join(names);
        }

        if (manga.getRelationships() != null) {
            for (Relationship r : manga.getRelationships()) {
                if (r == null || r.getAttributes() == null) continue;
                if ("author".equals(r.getType()) && r.getAttributes().getName() != null) {
                    author = r.getAttributes().getName();
                } else if ("cover_art".equals(r.getType())
                        && r.getAttributes().getFileName() != null) {
                    coverUrl = "https://uploads.mangadex.org/covers/"
                            + manga.getId() + "/" + r.getAttributes().getFileName();
                }
            }
        }

        return new Comic(manga.getId(), title, author, description, coverUrl,
                status, genres, rating, language, year, lastVolume, lastChapter);
    }

    private static String localized(Map<String, String> values) {
        if (values == null || values.isEmpty()) return null;
        String en = values.get("en");
        if (en != null && !en.trim().isEmpty()) return en;
        for (String value : values.values()) {
            if (value != null && !value.trim().isEmpty()) return value;
        }
        return null;
    }

    private static String join(List<String> values) {
        StringBuilder out = new StringBuilder();
        for (String value : values) {
            if (out.length() > 0) out.append(", ");
            out.append(value);
        }
        return out.toString();
    }
}
