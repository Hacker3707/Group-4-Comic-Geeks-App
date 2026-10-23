package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.GroupMemberViewHolder;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.UserDisplayUtils;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberViewHolder> {

    // Màn hình đăng ký lắng nghe khi bấm vào một thành viên
    public interface OnMemberClickListener {
        void onMemberClick(GroupMember member);
    }

    private final List<GroupMember> members = new ArrayList<>();
    // Tên + ảnh của các tài khoản (key = userId), cập nhật theo thời gian thực
    private final Map<String, User> users = new HashMap<>();
    private String currentUserId = null;
    private OnMemberClickListener onMemberClickListener;

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.onMemberClickListener = listener;
    }

    // Để hiện thêm chữ "(Bạn)" cạnh tên của chính mình
    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setUsers(Map<String, User> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.putAll(newUsers);
        }
        notifyDataSetChanged();
    }

    public void setMembers(List<GroupMember> newMembers) {
        members.clear();
        if (newMembers != null) {
            members.addAll(newMembers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_member_item, parent, false);
        return new GroupMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberViewHolder holder, int position) {
        final GroupMember member = members.get(position);

        User user = users.get(member.getUserId());
        String name = UserDisplayUtils.displayName(
                user, holder.itemView.getContext().getString(R.string.group_member_unknown_name));
        if (currentUserId != null && currentUserId.equals(member.getUserId())) {
            name = holder.itemView.getContext().getString(R.string.group_member_you, name);
        }
        String avatarUrl = null;
        if (user != null) {
            avatarUrl = user.getAvatarUrl();
        }
        holder.bind(member, name, avatarUrl);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMemberClickListener != null) {
                    onMemberClickListener.onMemberClick(member);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }
}
