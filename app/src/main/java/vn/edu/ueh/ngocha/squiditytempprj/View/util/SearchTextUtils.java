package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Chuẩn hoá chữ để tìm kiếm: không phân biệt hoa/thường và bỏ dấu tiếng Việt.
 * Ví dụ "Tình Yêu" và "tinh yeu" là như nhau.
 */
public final class SearchTextUtils {

    private SearchTextUtils() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String lower = text.toLowerCase(Locale.ROOT).trim();
        lower = lower.replace('đ', 'd');
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "");
    }

    // normalizedQuery phải là kết quả của normalize()
    public static boolean contains(String text, String normalizedQuery) {
        return normalize(text).contains(normalizedQuery);
    }
}
