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
    public RecyclerView rvPostImages;
    public TextView tvHashtag;
    public TextView tvCommentCount;
    public ImageView btnStamp;
    public EditText edtQuickComment;
    public ImageView btnSendComment;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        btnOptions = itemView.findViewById(R.id.btnOptions);
        btnFollow = itemView.findViewById(R.id.btnFollow);
        tvUserName = itemView.findViewById(R.id.tvUserName);
        tvHandle = itemView.findViewById(R.id.tvHandle);
        imgAvatar = itemView.findViewById(R.id.imgAvatar);

        tvPostContent = itemView.findViewById(R.id.tvPostContent);
        rvPostImages = itemView.findViewById(R.id.rvPostImages);

        tvHashtag = itemView.findViewById(R.id.tvHashtag);
        tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
        btnStamp = itemView.findViewById(R.id.btnStamp);

        edtQuickComment = itemView.findViewById(R.id.edtQuickComment);
        btnSendComment = itemView.findViewById(R.id.btnSendComment);
    }
}