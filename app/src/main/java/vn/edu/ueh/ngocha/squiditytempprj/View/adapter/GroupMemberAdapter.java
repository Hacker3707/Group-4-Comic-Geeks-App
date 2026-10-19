package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.GroupMemberViewHolder;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberViewHolder> {

    // Màn hình đăng ký lắng nghe khi bấm vào một thành viên
    public interface OnMemberClickListener {
        void onMemberClick(GroupMember member);
    }

    private final List<GroupMember> members = new ArrayList<>();
    private OnMemberClickListener onMemberClickListener;

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.onMemberClickListener = listener;
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
        holder.bind(member);
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
