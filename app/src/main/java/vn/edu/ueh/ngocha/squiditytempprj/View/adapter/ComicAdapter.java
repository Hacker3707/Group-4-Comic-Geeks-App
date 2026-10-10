package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.ComicViewHolder;

public class ComicAdapter extends RecyclerView.Adapter<ComicViewHolder>{
    private List<MangaData> mangaList = new ArrayList<>();

    public void setMangaList(List<MangaData> mangaList) {
        this.mangaList = mangaList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comic_item_view, parent, false);
        ComicViewHolder holder = new ComicViewHolder(view, this);
        return holder;
    }

    @Override
    public void onBindViewHolder(
            @NonNull ComicViewHolder holder, int position) {

        MangaData manga = mangaList.get(position);

        // Lấy tên truyện
        String title = "Untitled";

        if (manga.getAttributes() != null
                && manga.getAttributes().getTitle() != null) {

            if (manga.getAttributes().getTitle().get("en") != null) {
                title = manga.getAttributes().getTitle().get("en");
            } else if (manga.getAttributes().getTitle().get("ja-ro") != null) {
                title = manga.getAttributes().getTitle().get("ja-ro");
            } else if (!manga.getAttributes().getTitle().isEmpty()) {
                title = manga.getAttributes().getTitle()
                        .values().iterator().next();
            }
        }

        holder.txtcomicname.setText(title);

        // Lấy tên file cover từ relationship
        String coverUrl = null;

        if (manga.getRelationships() != null) {
            for (Relationship relationship : manga.getRelationships()) {

                if ("cover_art".equals(relationship.getType())
                        && relationship.getAttributes() != null
                        && relationship.getAttributes().getFileName() != null) {

                    coverUrl = "https://uploads.mangadex.org/covers/"
                            + manga.getId()
                            + "/"
                            + relationship.getAttributes().getFileName();

                    break;
                }
            }
        }

        // Load ảnh vào ImageView của từng item
        Glide.with(holder.itemView.getContext())
                .load(coverUrl)
                .placeholder(R.drawable.comic_background)
                .error(R.drawable.comic_background)
                .centerCrop()
                .into(holder.imgcomiccover);

    }

    @Override
    public int getItemCount() {
        return mangaList.size();
    }
}
