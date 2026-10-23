package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupRole;
import vn.edu.ueh.ngocha.squiditytempprj.R;

public class GroupMemberViewHolder extends RecyclerView.ViewHolder {

    private final ImageView imgMemberAvatar;
    private final TextView txtMemberName;
    private final TextView txtMemberRole;

    public GroupMemberViewHolder(@NonNull View itemView) {
        super(itemView);
        imgMemberAvatar = itemView.findViewById(R.id.imgMemberAvatar);
        txtMemberName = itemView.findViewById(R.id.txtMemberName);
        txtMemberRole = itemView.findViewById(R.id.txtMemberRole);
    }

    public void bind(GroupMember member, String displayName, String avatarUrl) {
        txtMemberName.setText(displayName);
        if (member.getRole() == GroupRole.ADMIN) {
            txtMemberRole.setText(R.string.group_role_admin);
        } else if (member.getRole() == GroupRole.MEMBER) {
            txtMemberRole.setText(R.string.group_role_member);
        } else {
            txtMemberRole.setText("");
        }

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.group_avatar_sample)
                    .error(R.drawable.group_avatar_sample)
                    .into(imgMemberAvatar);
        } else {
            Glide.with(itemView.getContext()).clear(imgMemberAvatar);
            imgMemberAvatar.setImageResource(R.drawable.group_avatar_sample);
        }
    }
}
