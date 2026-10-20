package vn.edu.ueh.ngocha.squiditytempprj.View.activity;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.PostAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.View.menu.BaseActivity;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;

public class SearchActivity extends BaseActivity {

    private EditText etGlobalSearch;
    private ImageButton btnBackSearch;
    private TabLayout searchTabLayout;
    private RecyclerView recyclerViewSearchResults;

    private PostViewModel postViewModel;
    private PostAdapter postAdapter;

    private int currentSearchType = 0; // 0: Post, 1: User, 2: Group

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etGlobalSearch = findViewById(R.id.etGlobalSearch);
        btnBackSearch = findViewById(R.id.btnBackSearch);
        searchTabLayout = findViewById(R.id.searchTabLayout);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);

        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, new ArrayList<>(), null);
        recyclerViewSearchResults.setAdapter(postAdapter);

        postViewModel.getAllPosts().observe(this, posts -> {
            String query = etGlobalSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query, currentSearchType);
            }
        });

        String initialQuery = getIntent().getStringExtra("SEARCH_QUERY");
        if (initialQuery != null && !initialQuery.isEmpty()) {
            etGlobalSearch.setText(initialQuery);
            etGlobalSearch.setSelection(initialQuery.length());
            performSearch(initialQuery, currentSearchType);
        }

        btnBackSearch.setOnClickListener(v -> finish());
        searchTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentSearchType = tab.getPosition();
                String query = etGlobalSearch.getText().toString().trim();
                performSearch(query, currentSearchType);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        etGlobalSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etGlobalSearch.getText().toString().trim();
                performSearch(query, currentSearchType);
                return true;
            }
            return false;
        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_search;
    }

    private void performSearch(String queryStr, int searchType) {
        if (queryStr == null || queryStr.isEmpty()) {
            postAdapter.setPosts(new ArrayList<>());
            return;
        }

        if (searchType == 0) {
            if (postViewModel != null && postViewModel.getAllPosts().getValue() != null) {
                List<Post> allPosts = postViewModel.getAllPosts().getValue();
                List<Post> filteredPosts = new ArrayList<>();

                String query = queryStr.toLowerCase().trim();

                for (Post p : allPosts) {
                    String content = p.getContent() != null ? p.getContent().toLowerCase() : "";
                    String hashtag = p.getHashtag() != null ? p.getHashtag().toLowerCase() : "";
                    String comicTitle = p.getComicTitle() != null ? p.getComicTitle().toLowerCase() : "";


                    if (hashtag.contains(query) || content.contains(query) || comicTitle.contains(query)) {
                        filteredPosts.add(p);
                    }
                }

                postAdapter.setPosts(filteredPosts);
                Toast.makeText(this, "Tìm thấy " + filteredPosts.size() + " bài viết", Toast.LENGTH_SHORT).show();
            }
        } else if (searchType == 1) {
            Toast.makeText(this, "Đang tìm User: " + queryStr, Toast.LENGTH_SHORT).show();
        } else if (searchType == 2) {
            Toast.makeText(this, "Đang tìm Group: " + queryStr, Toast.LENGTH_SHORT).show();
            //Ai làm khúc search hai ca user và Group sửa lại chỗ này nha hihi
        }
    }
}