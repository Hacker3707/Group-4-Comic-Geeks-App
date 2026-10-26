package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Đọc hashtag trong nội dung bài viết của group.
 * Hashtag nằm ngay trong nội dung (ví dụ "Tôi thích anime này quá #Anime #Manga")
 * nên không cần thêm cột vào database.
 *
 * Quy tắc:
 * - Chủ đề hợp lệ: Anime, Manga, LightNovel, Discussion, Otaku (không phân biệt hoa thường).
 * - Bài không có hashtag nào hợp lệ (không có # hoặc # sai) được xếp vào mục "Khác".
 */
public final class GroupHashtagUtils {

    // Khoá của từng chủ đề (trùng với android:tag của các chip trong group_view_activity.xml)
    public static final String TAG_ANIME = "anime";
    public static final String TAG_MANGA = "manga";
    public static final String TAG_LIGHT_NOVEL = "lightnovel";
    public static final String TAG_DISCUSSION = "discussion";
    public static final String TAG_OTAKU = "otaku";
    public static final String TAG_OTHER = "other";

    private static final String[] VALID_TAGS = {
            TAG_ANIME, TAG_MANGA, TAG_LIGHT_NOVEL, TAG_DISCUSSION, TAG_OTAKU
    };

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_]+)");

    private GroupHashtagUtils() {
    }

    // Chữ thường, bỏ dấu _ và - để "Light_Novel", "light-novel", "LightNovel" đều giống nhau
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    }

    // Trả về khoá chủ đề của một hashtag, hoặc null nếu hashtag không nằm trong danh sách
    private static String toValidTag(String hashtagText) {
        String normalized = normalize(hashtagText);
        for (String validTag : VALID_TAGS) {
            if (validTag.equals(normalized)) {
                return validTag;
            }
        }
        return null;
    }

    // Các chủ đề của một bài viết. Không có chủ đề hợp lệ nào thì trả về "other"
    public static Set<String> extractTags(String content) {
        Set<String> tags = new LinkedHashSet<>();
        if (content != null) {
            Matcher matcher = HASHTAG_PATTERN.matcher(content);
            while (matcher.find()) {
                String validTag = toValidTag(matcher.group(1));
                if (validTag != null) {
                    tags.add(validTag);
                }
            }
        }
        if (tags.isEmpty()) {
            tags.add(TAG_OTHER);
        }
        return tags;
    }

    // Tên hiển thị của một chủ đề
    public static String displayName(String tagKey) {
        if (TAG_ANIME.equals(tagKey)) {
            return "Anime";
        } else if (TAG_MANGA.equals(tagKey)) {
            return "Manga";
        } else if (TAG_LIGHT_NOVEL.equals(tagKey)) {
            return "Light Novel";
        } else if (TAG_DISCUSSION.equals(tagKey)) {
            return "Discussion";
        } else if (TAG_OTAKU.equals(tagKey)) {
            return "Otaku";
        }
        return Str.t(R.string.group_chip_other);
    }

    // Nhãn chủ đề của một bài viết, ví dụ "Manga, Anime" hoặc "Khác"
    public static String topicLabel(String content) {
        StringBuilder label = new StringBuilder();
        for (String tagKey : extractTags(content)) {
            if (label.length() > 0) {
                label.append(", ");
            }
            label.append(displayName(tagKey));
        }
        return label.toString();
    }

    public static boolean hasTag(String content, String tagKey) {
        return extractTags(content).contains(tagKey);
    }

    // "#LightNovel" có bắt đầu bằng phần người dùng đang gõ sau dấu # (ví dụ "light") không
    public static boolean tagStartsWith(String suggestionTag, String typedAfterHash) {
        if (suggestionTag == null) {
            return false;
        }
        String tagWithoutHash = suggestionTag.startsWith("#")
                ? suggestionTag.substring(1) : suggestionTag;
        return normalize(tagWithoutHash).startsWith(normalize(typedAfterHash));
    }

    // Tô màu các hashtag trong nội dung bài viết
    public static CharSequence highlight(String content, int color) {
        if (content == null) {
            return "";
        }
        SpannableString spannable = new SpannableString(content);
        Matcher matcher = HASHTAG_PATTERN.matcher(content);
        while (matcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(color),
                    matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }
}
