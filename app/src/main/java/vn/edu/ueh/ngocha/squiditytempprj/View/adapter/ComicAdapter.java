package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.ComicViewHolder;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;

public class ComicAdapter extends RecyclerView.Adapter<ComicViewHolder> {
    private List<MangaData> mangaList = new ArrayList<>();
    private List<Comic> comicList = new ArrayList<>();
    private boolean showingRoomData = false;

    public interface OnComicClickListener {
        void onComicClick(String comicId, MangaData mangaData);
    }

    private final OnComicClickListener listener;

    public ComicAdapter(OnComicClickListener listener) {
        this.listener = listener;
    }

    public void setMangaList(List<MangaData> list) {
        showingRoomData = false;
        mangaList = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    public void setComicList(List<Comic> list) {
        showingRoomData = true;
        comicList = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comic_item_view, parent, false);
        int orientation = parent.getContext().getResources().getConfiguration().orientation;
        return new ComicViewHolder(view, orientation);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        if (showingRoomData) {
            Comic comic = comicList.get(position);
            holder.txtcomicname.setText(valueOrUnknown(comic.getComicName()));
            if (holder.txtcomicauthor != null) {
                holder.txtcomicauthor.setText(valueOrUnknown(comic.getComicAuthor()));
            }
            Glide.with(holder.itemView.getContext())
                    .load(comic.getCoverImageUrl())
                    .placeholder(R.drawable.comic_background)
                    .error(R.drawable.comic_background)
                    .centerCrop()
                    .into(holder.imgcomiccover);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onComicClick(comic.getComicId(), null);
            });
            return;
        }

        MangaData manga = mangaList.get(position);
        String title = Str.t(R.string.comic_untitled);
        if (manga.getAttributes() != null && manga.getAttributes().getTitle() != null) {
            title = localized(manga.getAttributes().getTitle(), title);
        }
        holder.txtcomicname.setText(title);

        String coverUrl = null;
        String authorName = Str.t(R.string.common_unknown);
        if (manga.getRelationships() != null) {
            for (Relationship r : manga.getRelationships()) {
                if (r == null || r.getAttributes() == null) continue;
                if ("cover_art".equals(r.getType()) && r.getAttributes().getFileName() != null) {
                    coverUrl = "https://uploads.mangadex.org/covers/" + manga.getId()
                            + "/" + r.getAttributes().getFileName();
                } else if ("author".equals(r.getType()) && r.getAttributes().getName() != null) {
                    authorName = r.getAttributes().getName();
                }
            }
        }
        if (holder.txtcomicauthor != null) holder.txtcomicauthor.setText(authorName);
        Glide.with(holder.itemView.getContext())
                .load(coverUrl)
                .placeholder(R.drawable.comic_background)
                .error(R.drawable.comic_background)
                .centerCrop()
                .into(holder.imgcomiccover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && manga.getId() != null) {
                listener.onComicClick(manga.getId(), manga);
            }
        });
    }

    private String localized(Map<String, String> map, String fallback) {
        String en = map.get("en");
        if (en != null && !en.trim().isEmpty()) return en;
        String romanized = map.get("ja-ro");
        if (romanized != null && !romanized.trim().isEmpty()) return romanized;
        for (String value : map.values()) {
            if (value != null && !value.trim().isEmpty()) return value;
        }
        return fallback;
    }

    private String valueOrUnknown(String value) {
        return value == null || value.trim().isEmpty()
                ? Str.t(R.string.common_unknown) : value;
    }

    @Override
    public int getItemCount() {
        return showingRoomData ? comicList.size() : mangaList.size();
    }
}
