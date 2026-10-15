package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.ueh.ngocha.squiditytempprj.R;

public class PostViewHolder extends RecyclerView.ViewHolder {
    public ImageView btnOptions;
    public Button btnFollow;
    public TextView tvUserName;
    public TextView tvHandle;
    public ImageView imgAvatar;
    public TextView tvPostContent;
    public ImageView imgPostImage;
    public TextView tvHashtag;
    public TextView tvCommentCount;
    public ImageView btnStamp;
    public EditText edtQuickComment;
    public ImageView btnSendComment;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);

        // 1. Ánh xạ Header
        btnOptions = itemView.findViewById(R.id.btnOptions);
        btnFollow = itemView.findViewById(R.id.btnFollow);
        tvUserName = itemView.findViewById(R.id.tvUserName);
        tvHandle = itemView.findViewById(R.id.tvHandle);
        imgAvatar = itemView.findViewById(R.id.imgAvatar);

        // 2. Ánh xạ Content
        tvPostContent = itemView.findViewById(R.id.tvPostContent);
        imgPostImage = itemView.findViewById(R.id.imgPostImage);

        // 3. Ánh xạ Actions
        tvHashtag = itemView.findViewById(R.id.tvHashtag);
        tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
        btnStamp = itemView.findViewById(R.id.btnStamp);

        // 4. Ánh xạ Comment
        edtQuickComment = itemView.findViewById(R.id.edtQuickComment);
        btnSendComment = itemView.findViewById(R.id.btnSendComment);
    }
}