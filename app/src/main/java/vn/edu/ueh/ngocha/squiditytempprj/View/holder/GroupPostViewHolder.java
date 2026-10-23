package vn.edu.ueh.ngocha.squiditytempprj.View.holder;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupHashtagUtils;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.UserDisplayUtils;

public class GroupPostViewHolder extends RecyclerView.ViewHolder {

    // Báo cho adapter nhớ chữ đang gõ dở trong ô bình luận (để không mất khi danh sách vẽ lại)
    public interface OnDraftChangedListener {
        void onDraftChanged(String text);
    }

    private final ImageView imgPostAvatar;
    private final TextView txtAuthorName;
    private final TextView txtAuthorHandle;
    private final TextView txtContent;
    private final TextView txtTopic;

    private final LinearLayout layoutCommentList;
    private final TextView txtSay;
    private final LinearLayout layoutCommentInput;
    private final EditText edtComment;
    private final ImageView btnSendComment;

    private OnDraftChangedListener draftListener;
    private boolean binding = false;

    public GroupPostViewHolder(@NonNull View itemView) {
        super(itemView);
        imgPostAvatar = itemView.findViewById(R.id.imgPostAvatar);
        txtAuthorName = itemView.findViewById(R.id.txtAuthorName);
        txtAuthorHandle = itemView.findViewById(R.id.txtAuthorHandle);
        txtContent = itemView.findViewById(R.id.txtContent);
        txtTopic = itemView.findViewById(R.id.txtTopic);

        layoutCommentList = itemView.findViewById(R.id.layoutCommentList);
        txtSay = itemView.findViewById(R.id.txtSay);
        layoutCommentInput = itemView.findViewById(R.id.layoutCommentInput);
        edtComment = itemView.findViewById(R.id.edtComment);
        btnSendComment = itemView.findViewById(R.id.btnSendComment);

        edtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!binding && draftListener != null) {
                    draftListener.onDraftChanged(s.toString());
                }
            }
        });
    }

    public void bind(Post post, String authorName, String authorHandle, String avatarUrl) {
        txtAuthorName.setText(authorName);
        txtAuthorHandle.setText(authorHandle);
        int hashtagColor = ContextCompat.getColor(itemView.getContext(), R.color.group_tab_selected);
        txtContent.setText(GroupHashtagUtils.highlight(post.getContent(), hashtagColor));

        // Chủ đề lấy theo hashtag trong bài (không có hashtag hợp lệ thì là "Khác")
        txtTopic.setText(GroupHashtagUtils.topicLabel(post.getContent()));

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.group_avatar_sample)
                    .error(R.drawable.group_avatar_sample)
                    .into(imgPostAvatar);
        } else {
            Glide.with(itemView.getContext()).clear(imgPostAvatar);
            imgPostAvatar.setImageResource(R.drawable.group_avatar_sample);
        }
    }

    // =================================
    // Bình luận
    // =================================

    // Vẽ các bình luận (thẻ nhỏ hơn thẻ bài) nằm trong khung bài viết
    public void bindComments(List<Comment> comments, Map<String, User> users, String fallbackName) {
        layoutCommentList.removeAllViews();
        if (comments == null || comments.isEmpty()) {
            layoutCommentList.setVisibility(View.GONE);
            return;
        }
        layoutCommentList.setVisibility(View.VISIBLE);

        Context context = itemView.getContext();
        for (Comment comment : comments) {
            String name = UserDisplayUtils.displayName(users.get(comment.getUserId()), fallbackName);
            layoutCommentList.addView(createCommentView(context, name, comment.getContent()));
        }
    }

    private View createCommentView(Context context, String name, String content) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_group_comment_card);
        int pad = dp(context, 8);
        card.setPadding(pad, pad, pad, pad);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // Thụt vào một chút để nhìn ra đây là bình luận, không phải bài đăng
        cardParams.setMargins(dp(context, 14), 0, 0, dp(context, 6));
        card.setLayoutParams(cardParams);

        TextView txtName = new TextView(context);
        txtName.setText(name);
        txtName.setTextColor(ContextCompat.getColor(context, R.color.group_name));
        txtName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        txtName.setTypeface(ResourcesCompat.getFont(context, R.font.inter_bold));
        card.addView(txtName);

        TextView txtText = new TextView(context);
        txtText.setText(content);
        txtText.setTextColor(ContextCompat.getColor(context, R.color.group_text));
        txtText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        card.addView(txtText);

        return card;
    }

    private int dp(Context context, int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }

    // open = true: hiện ô nhập bình luận thay cho dòng "Say something..."
    public void bindCommentInput(boolean open, String draft, OnDraftChangedListener listener) {
        draftListener = listener;
        binding = true;
        txtSay.setVisibility(open ? View.GONE : View.VISIBLE);
        layoutCommentInput.setVisibility(open ? View.VISIBLE : View.GONE);
        if (open) {
            edtComment.setText(draft == null ? "" : draft);
            edtComment.setSelection(edtComment.length());
        }
        binding = false;
    }

    public View getSayView() {
        return txtSay;
    }

    public View getSendCommentView() {
        return btnSendComment;
    }

    public String getCommentText() {
        return edtComment.getText().toString().trim();
    }

    public void focusCommentInput() {
        edtComment.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(edtComment, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(edtComment.getWindowToken(), 0);
        }
    }
}
