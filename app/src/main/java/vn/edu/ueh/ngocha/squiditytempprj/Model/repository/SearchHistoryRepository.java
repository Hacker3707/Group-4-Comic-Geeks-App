package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SearchHistoryRepository {

    private static final String PREF_NAME = "search_history";
    private static final String KEY_HISTORY = "history";
    private static final String HOME_KEY_HISTORY = "home_history";
    private static final int MAX_HISTORY = 10;

    private final SharedPreferences preferences;

    public SearchHistoryRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );
    }

    public List<String> getcomicHistory() {
        Set<String> savedHistory = preferences.getStringSet(
                KEY_HISTORY,
                new LinkedHashSet<String>()
        );

        List<String> history = new ArrayList<>(savedHistory);

        // Từ khóa mới nhất nằm ở đầu danh sách
        Collections.reverse(history);

        return history;
    }

    public List<String> gethomeHistory() {
        Set<String> savedHistory = preferences.getStringSet(
                HOME_KEY_HISTORY,
                new LinkedHashSet<String>());

        List<String> history = new ArrayList<>(savedHistory);

        // Từ khóa mới nhất nằm ở đầu danh sách
        Collections.reverse(history);

        return history;
    }

    public void savecomicSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        String keyword = query.trim();

        Set<String> savedHistory = preferences.getStringSet(
                KEY_HISTORY,
                new LinkedHashSet<String>()
        );

        LinkedHashSet<String> history =
                new LinkedHashSet<>(savedHistory);

        history.remove(keyword);
        history.add(keyword);

        while (history.size() > MAX_HISTORY) {
            String oldest = history.iterator().next();
            history.remove(oldest);
        }

        preferences.edit()
                .putStringSet(KEY_HISTORY, history)
                .apply();
    }

    public void savehomeSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        String keyword = query.trim();

        Set<String> savedHistory = preferences.getStringSet(
                HOME_KEY_HISTORY,
                new LinkedHashSet<String>()
        );

        LinkedHashSet<String> history =
                new LinkedHashSet<>(savedHistory);

        history.remove(keyword);
        history.add(keyword);

        while (history.size() > MAX_HISTORY) {
            String oldest = history.iterator().next();
            history.remove(oldest);
        }

        preferences.edit()
                .putStringSet(HOME_KEY_HISTORY, history)
                .apply();
    }

    public void clearcomicHistory() {
        preferences.edit()
                .remove(KEY_HISTORY)
                .apply();
    }

    public void clearhomeHistory() {
        preferences.edit()
                .remove(HOME_KEY_HISTORY)
                .apply();
    }

}