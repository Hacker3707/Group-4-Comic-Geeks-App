package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.GroupViewHolder;

/**
 * Adapter hiển thị danh sách group (mỗi dòng dùng layout group_item_view).
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupViewHolder> {

    // Màn hình đăng ký lắng nghe khi người dùng bấm vào một group
    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    private final List<Group> groups = new ArrayList<>();
    private OnGroupClickListener onGroupClickListener;

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.onGroupClickListener = listener;
    }

    public void setGroups(List<Group> newGroups) {
        groups.clear();
        if (newGroups != null) {
            groups.addAll(newGroups);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_item_view, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        final Group group = groups.get(position);
        holder.bind(group);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onGroupClickListener != null) {
                    onGroupClickListener.onGroupClick(group);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }
}
