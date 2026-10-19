package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.R;

public class GroupViewHolder extends RecyclerView.ViewHolder {

    private final ImageView imgGroupCover;
    private final TextView txtGroupLetter;
    private final TextView txtGroupName;
    private final TextView txtGroupDescription;

    public GroupViewHolder(@NonNull View itemView) {
        super(itemView);
        imgGroupCover = itemView.findViewById(R.id.imgGroupCover);
        txtGroupLetter = itemView.findViewById(R.id.txtGroupLetter);
        txtGroupName = itemView.findViewById(R.id.txtGroupName);
        txtGroupDescription = itemView.findViewById(R.id.txtGroupDescription);
    }

    public void bind(Group group) {
        txtGroupName.setText(group.getGroupName());
        txtGroupDescription.setText(group.getDescription());

        String coverUrl = group.getCoverImageUrl();
        if (coverUrl == null || coverUrl.trim().isEmpty()) {
            // Không có ảnh bìa thì dùng ảnh đại diện của nhóm
            coverUrl = group.getAvatarUrl();
        }
        if (coverUrl != null && !coverUrl.trim().isEmpty()) {
            // Có link ảnh bìa: hiện ảnh của nhóm
            txtGroupLetter.setVisibility(View.GONE);
            Glide.with(itemView.getContext())
                    .load(coverUrl.trim())
                    .placeholder(R.drawable.group_cover_placeholder)
                    .error(R.drawable.group_cover_placeholder)
                    .centerCrop()
                    .into(imgGroupCover);
        } else {
            // Chưa có ảnh: nền tím + chữ cái đầu của tên nhóm
            imgGroupCover.setImageResource(R.drawable.group_cover_placeholder);
            String name = group.getGroupName();
            if (name == null || name.trim().isEmpty()) {
                txtGroupLetter.setText("?");
            } else {
                txtGroupLetter.setText(name.trim().substring(0, 1).toUpperCase());
            }
            txtGroupLetter.setVisibility(View.VISIBLE);
        }
    }
}
