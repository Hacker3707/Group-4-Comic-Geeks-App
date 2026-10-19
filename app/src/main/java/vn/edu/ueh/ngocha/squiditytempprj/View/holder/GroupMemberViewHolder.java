package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupRole;
import vn.edu.ueh.ngocha.squiditytempprj.R;

public class GroupMemberViewHolder extends RecyclerView.ViewHolder {

    private final TextView txtMemberName;
    private final TextView txtMemberRole;

    public GroupMemberViewHolder(@NonNull View itemView) {
        super(itemView);
        txtMemberName = itemView.findViewById(R.id.txtMemberName);
        txtMemberRole = itemView.findViewById(R.id.txtMemberRole);
    }

    public void bind(GroupMember member) {
        txtMemberName.setText(member.getUserId());
        if (member.getRole() == GroupRole.ADMIN) {
            txtMemberRole.setText(R.string.group_role_admin);
        } else if (member.getRole() == GroupRole.MEMBER) {
            txtMemberRole.setText(R.string.group_role_member);
        } else {
            txtMemberRole.setText("");
        }
    }
}
