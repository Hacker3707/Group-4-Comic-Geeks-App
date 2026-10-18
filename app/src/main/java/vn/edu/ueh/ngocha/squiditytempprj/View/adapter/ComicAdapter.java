package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.MangaData;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dto.Relationship;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.ComicViewHolder;

public class ComicAdapter extends RecyclerView.Adapter<ComicViewHolder> {
    private List<MangaData> mangaList = new ArrayList<>();

    public interface OnComicClickListener {
        void onComicClick(String comicId);
    }

    private OnComicClickListener listener;

    public ComicAdapter(OnComicClickListener listener) {
        this.listener = listener;
    }

    public void setMangaList(List<MangaData> mangaList) {
        this.mangaList = mangaList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comic_item_view, parent, false);
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        MangaData manga = mangaList.get(position);

        String title = "Untitled";
        if (manga.getAttributes() != null && manga.getAttributes().getTitle() != null) {
            if (manga.getAttributes().getTitle().get("en") != null) {
                title = manga.getAttributes().getTitle().get("en");
            } else if (manga.getAttributes().getTitle().get("ja-ro") != null) {
                title = manga.getAttributes().getTitle().get("ja-ro");
            } else if (!manga.getAttributes().getTitle().isEmpty()) {
                title = manga.getAttributes().getTitle().values().iterator().next();
            }
        }

        holder.txtcomicname.setText(title);

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

        Glide.with(holder.itemView.getContext())
                .load(coverUrl)
                .placeholder(R.drawable.comic_background)
                .error(R.drawable.comic_background)
                .centerCrop()
                .into(holder.imgcomiccover);

        // BẮT SỰ KIỆN CLICK TRỰC TIẾP TRÊN TỪNG ITEM
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && manga.getId() != null) {
                    listener.onComicClick(manga.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mangaList == null) {
            return 0;
        }
        return mangaList.size();
    }
}