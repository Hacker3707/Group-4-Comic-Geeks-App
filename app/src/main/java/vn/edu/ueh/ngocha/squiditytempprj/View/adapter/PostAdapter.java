package vn.edu.ueh.ngocha.squiditytempprj.View.adapter;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.FollowRemoteRepository;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ComicDetailActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.CreatePostActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.GroupViewActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ProfileActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.SearchActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.holder.PostViewHolder;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.PostViewModel;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.GroupHashtagUtils;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.UserDisplayUtils;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_POST = 0;
    private static final int TYPE_GROUP_PAGE_POST = 1;

    public interface OnCommentSendListener {
        void onCommentSend(Post post, String content);
    }

    private List<Post> postList;
    private final Context context;
    private OnPostInteractionListener listener;
    private final boolean groupPageMode;

    private final Map<String, Boolean> expandedStates = new HashMap<>();

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Group> groups = new HashMap<>();

    private final Map<String, List<Comment>> commentsByPost = new HashMap<>();
    private OnCommentSendListener commentSendListener;
    private String commentingPostId = null;
    private String commentDraft = "";

    private static final String PAYLOAD_FOLLOW = "follow_state";
    private final Set<String> myFollowingIds = new HashSet<>();
    private FollowRemoteRepository followRepository;
    private ListenerRegistration followRegistration;

    public interface OnPostInteractionListener {
        void onStampClick(Post post);
        void onCommentSend(Post post, String commentText);
        void onViewCommentsClick(Post post);
        void onEditPost(Post post);
        void onDeletePost(Post post);
    }

    public PostAdapter(Context context, List<Post> postList, OnPostInteractionListener listener) {
        this.context = context;
        this.postList = postList != null ? postList : new ArrayList<Post>();
        this.listener = listener;
        this.groupPageMode = false;
    }

    private PostAdapter(Context context, OnPostInteractionListener listener) {
        this.context = context;
        this.postList = new ArrayList<>();
        this.listener = listener;
        this.groupPageMode = true;
    }

    public static PostAdapter forGroupPage(Context context, OnPostInteractionListener listener) {
        return new PostAdapter(context, listener);
    }

    public void setOnCommentSendListener(OnCommentSendListener listener) {
        this.commentSendListener = listener;
    }

    public void setUsers(Map<String, User> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.putAll(newUsers);
        }
        notifyDataSetChanged();
    }

    public void setGroups(Map<String, Group> newGroups) {
        groups.clear();
        if (newGroups != null) {
            groups.putAll(newGroups);
        }
        notifyDataSetChanged();
    }

    public void setComments(Map<String, List<Comment>> newComments) {
        commentsByPost.clear();
        if (newComments != null) {
            commentsByPost.putAll(newComments);
        }
        notifyDataSetChanged();
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts != null ? new ArrayList<>(posts) : new ArrayList<Post>();
        notifyDataSetChanged();
    }

    private String fallbackName() {
        return context.getString(R.string.group_member_unknown_name);
    }

    private String authorName(Post post) {
        User user = users.get(post.getUserId());
        return UserDisplayUtils.displayName(user,
                post.getUserId() != null ? post.getUserId() : fallbackName());
    }

    private static boolean isGroupPost(Post post) {
        return !TextUtils.isEmpty(post.getGroupId());
    }

    // =================================
    // Follow & Profile Navigation
    // =================================

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null || followRegistration != null) {
            return;
        }
        followRepository = new FollowRemoteRepository((Application) context.getApplicationContext());
        followRegistration = followRepository.listenFollowingIds(me.getUid(),
                new FollowRemoteRepository.Callback<Set<String>>() {
                    @Override
                    public void onResult(Set<String> ids) {
                        myFollowingIds.clear();
                        myFollowingIds.addAll(ids);
                        notifyItemRangeChanged(0, getItemCount(), PAYLOAD_FOLLOW);
                    }
                });
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (followRegistration != null) {
            followRegistration.remove();
            followRegistration = null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.contains(PAYLOAD_FOLLOW) && holder instanceof PostViewHolder) {
            bindFollowButton((PostViewHolder) holder, postList.get(position));
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    private void bindFollowButton(PostViewHolder holder, Post post) {
        if (holder.btnFollow == null) return;

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        final String ownerId = post.getUserId();

        if (me == null || ownerId == null || ownerId.isEmpty() || ownerId.equals(me.getUid())) {
            holder.btnFollow.setVisibility(View.GONE);
            return;
        }

        holder.btnFollow.setVisibility(View.VISIBLE);
        holder.btnFollow.setText(myFollowingIds.contains(ownerId) ? Str.t(R.string.common_following) : Str.t(R.string.common_follow_plus));
        holder.btnFollow.setOnClickListener(v -> {
            if (followRepository == null) return;
            if (myFollowingIds.contains(ownerId)) {
                followRepository.unfollow(me.getUid(), ownerId);
            } else {
                followRepository.follow(me.getUid(), ownerId);
            }
        });
    }

    // 👉 Hàm mở trang Profile cá nhân của người dùng
    private void openProfile(String userId) {
        if (userId == null || userId.isEmpty()) return;
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("profile_user_id", userId);
        intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PostViewHolder(inflater.inflate(R.layout.activity_post, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindPost((PostViewHolder) holder, postList.get(position), position);
    }

    // =================================
    // Tách nhỏ các hàm bind để dọn gọn code
    // =================================

    private void bindHeader(PostViewHolder holder, Post post) {
        Group group = (!groupPageMode && isGroupPost(post)) ? groups.get(post.getGroupId()) : null;
        String name = authorName(post);
        User user = users.get(post.getUserId());
        String posterAvatarUrl = user != null ? user.getAvatarUrl() : null;
        final String ownerId = post.getUserId();

        if (group != null) {
            holder.headerLayout.setVisibility(View.GONE);
            holder.groupHeaderLayout.setVisibility(View.VISIBLE);

            String groupName = group.getGroupName();
            holder.tvGroupName.setText(groupName != null ? groupName : "");
            holder.tvGroupPoster.setText(name);

            loadAvatar(holder.imgGroupAvatar, group.getAvatarUrl());
            loadAvatar(holder.imgGroupPosterAvatar, posterAvatarUrl);

            final String groupId = group.getGroupId();
            View.OnClickListener openGroup = v -> {
                Intent intent = new Intent(context, GroupViewActivity.class);
                intent.putExtra(GroupViewActivity.EXTRA_GROUP_ID, groupId);
                context.startActivity(intent);
            };
            holder.imgGroupAvatar.setOnClickListener(openGroup);
            holder.tvGroupName.setOnClickListener(openGroup);

            // Bấm vào avatar/tên người đăng nhỏ trong group -> Sang Profile người đó
            View.OnClickListener openOwner = v -> openProfile(ownerId);
            holder.imgGroupPosterAvatar.setOnClickListener(openOwner);
            holder.tvGroupPoster.setOnClickListener(openOwner);
        } else {
            holder.groupHeaderLayout.setVisibility(View.GONE);
            holder.headerLayout.setVisibility(View.VISIBLE);
            holder.tvUserName.setText(name);
            if (holder.tvHandle != null) {
                holder.tvHandle.setText(user != null ? "@" + name.replace(" ", "").toLowerCase() : "");
            }
            loadAvatar(holder.imgAvatar, posterAvatarUrl);
            bindFollowButton(holder, post);

            // 👉 Bấm vào tên hoặc avatar chính của bài đăng -> Sang Profile người đó
            View.OnClickListener openOwnerProfile = v -> openProfile(ownerId);
            holder.tvUserName.setOnClickListener(openOwnerProfile);
            if (holder.imgAvatar != null) {
                holder.imgAvatar.setOnClickListener(openOwnerProfile);
            }
        }
    }

    private void loadAvatar(ImageView view, String url) {
        if (url != null && !url.trim().isEmpty()) {
            Glide.with(context)
                    .load(url.trim())
                    .placeholder(R.drawable.group_avatar_sample)
                    .error(R.drawable.group_avatar_sample)
                    .centerCrop()
                    .into(view);
        } else {
            Glide.with(context).clear(view);
            view.setImageResource(R.drawable.group_avatar_sample);
        }
    }

    private void bindPost(@NonNull final PostViewHolder holder, final Post post, final int position) {
        bindHeader(holder, post);
        bindCommentCount(holder, post);
        bindContent(holder, post, position);
        bindLinkedComic(holder, post);
        bindHashtag(holder, post);
        setupInteractions(holder, post, position);
        bindImages(holder, post);
    }

    private void bindCommentCount(PostViewHolder holder, Post post) {
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            PostViewModel postViewModel = new ViewModelProvider(activity).get(PostViewModel.class);
            if (post.getPostId() != null) {
                postViewModel.getCommentsByPostId(post.getPostId()).observe(activity, comments -> {
                    if (comments != null) {
                        int realCount = comments.size();
                        holder.tvCommentCount.setText(String.valueOf(realCount));
                        post.setCommentCount(realCount);
                    } else {
                        holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
                    }
                });
            } else {
                holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
            }
        } else {
            holder.tvCommentCount.setText(String.valueOf(post.getCommentCount()));
        }
    }

    private void bindContent(PostViewHolder holder, Post post, int position) {
        String content = post.getContent();
        String postId = post.getPostId() != null ? post.getPostId() : String.valueOf(position);
        boolean isExpanded = expandedStates.containsKey(postId) && Boolean.TRUE.equals(expandedStates.get(postId));

        if (content != null && !content.isEmpty()) {
            holder.tvPostContent.setVisibility(View.VISIBLE);

            if (isExpanded) {
                holder.tvPostContent.setMaxLines(Integer.MAX_VALUE);
                holder.tvPostContent.setEllipsize(null);
                holder.tvPostContent.setText(Str.t(R.string.post_collapse, content));
            } else {
                holder.tvPostContent.setMaxLines(3);
                holder.tvPostContent.setEllipsize(TextUtils.TruncateAt.END);
                holder.tvPostContent.setText(content);
            }

            holder.tvPostContent.setOnClickListener(v -> {
                boolean currentExpanded = expandedStates.containsKey(postId) && Boolean.TRUE.equals(expandedStates.get(postId));
                expandedStates.put(postId, !currentExpanded);
                notifyItemChanged(position);
            });
        } else {
            holder.tvPostContent.setVisibility(View.GONE);
        }
    }

    private void bindLinkedComic(PostViewHolder holder, Post post) {
        if (holder.tvLinkedComic != null) {
            String comicTitle = post.getComicTitle();
            String comicId = post.getComicId();

            if (comicTitle != null && !comicTitle.isEmpty()) {
                holder.tvLinkedComic.setVisibility(View.VISIBLE);
                holder.tvLinkedComic.setText("📖 " + comicTitle);

                holder.tvLinkedComic.setOnClickListener(v -> {
                    if (comicId != null && !comicId.isEmpty() && !comicId.equals("Discussion")) {
                        Intent intent = new Intent(context, ComicDetailActivity.class);
                        intent.putExtra("comic_id", comicId);
                        intent.putExtra("search_option", "MangaDex");
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, Str.t(R.string.post_comic_id_not_found), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.tvLinkedComic.setVisibility(View.GONE);
            }
        }
    }

    private void bindHashtag(PostViewHolder holder, Post post) {
        final String hashtagToDisplay;

        if (isGroupPost(post)) {
            String content = post.getContent();
            String foundHashtag = "";

            // 1. Ưu tiên quét xem trong nội dung (content) có chứa từ bắt đầu bằng dấu # hay không
            if (content != null && content.contains("#")) {
                int hashIndex = content.indexOf("#");
                String sub = content.substring(hashIndex).trim();
                int spaceIndex = sub.indexOf(" ");
                if (spaceIndex != -1) {
                    foundHashtag = sub.substring(0, spaceIndex);
                } else {
                    foundHashtag = sub;
                }
            }

            // 2. Nếu trong content không có dấu #, kiểm tra tiếp trường post.getHashtag() xem có dữ liệu không
            if (foundHashtag.isEmpty() && post.getHashtag() != null && !post.getHashtag().trim().isEmpty()) {
                foundHashtag = post.getHashtag().trim();
                if (!foundHashtag.startsWith("#")) {
                    foundHashtag = "#" + foundHashtag;
                }
            }

            // 3. Nếu vẫn không có nữa thì mới dùng bộ lọc topicLabel hoặc mặc định là #Discussion
            if (foundHashtag.isEmpty()) {
                String topic = GroupHashtagUtils.topicLabel(content);
                foundHashtag = (topic != null && !topic.isEmpty() && !topic.equals(Str.t(R.string.group_chip_other))) ? "#" + topic : "#Discussion";
            }

            holder.tvHashtag.setText(foundHashtag);
            hashtagToDisplay = foundHashtag;
        } else {
            // Dành cho bài đăng ngoài trang chủ (Home)
            String hashtag = post.getHashtag();
            if (hashtag != null && !hashtag.trim().isEmpty()) {
                hashtagToDisplay = hashtag.startsWith("#") ? hashtag : "#" + hashtag;
            } else {
                hashtagToDisplay = "#Discussion";
            }
            holder.tvHashtag.setText(hashtagToDisplay);
        }

        // Sự kiện khi bấm vào hashtag để tìm kiếm
        holder.tvHashtag.setOnClickListener(v -> {
            Intent intent = new Intent(context, SearchActivity.class);
            intent.putExtra("SEARCH_QUERY", hashtagToDisplay.replace("#", "").trim());
            context.startActivity(intent);
        });
    }

    private void setupInteractions(PostViewHolder holder, Post post, int position) {
        holder.btnStamp.setOnClickListener(v -> {
            boolean isStamped = holder.btnStamp.getTag() != null && (boolean) holder.btnStamp.getTag();
            if (isStamped) {
                holder.btnStamp.setColorFilter(android.graphics.Color.parseColor("#A890D3"));
                holder.btnStamp.setImageResource(R.drawable.ic_splash_unfill);
                holder.btnStamp.setTag(false);
            } else {
                holder.btnStamp.setColorFilter(android.graphics.Color.parseColor("#493373"));
                holder.btnStamp.setImageResource(R.drawable.post_ink_splat);
                holder.btnStamp.setTag(true);

                if (listener != null) {
                    listener.onStampClick(post);
                }
            }
        });

        holder.tvCommentCount.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewCommentsClick(post);
            }
        });

        holder.btnSendComment.setOnClickListener(v -> {
            String commentText = holder.edtQuickComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                if (listener != null) listener.onCommentSend(post, commentText);
                holder.edtQuickComment.setText("");
                notifyItemChanged(position);
            }
        });

        // 👉 KIỂM TRA QUYỀN SỞ HỮU BÀI VIẾT (So sánh Firebase UID của bài post với user đang đăng nhập)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        View.OnClickListener openOptions = v -> showOptionsMenu(v, post);

        boolean canModify = post.getUserId() != null && currentUser != null &&
                post.getUserId().equals(currentUser.getUid());

        if (canModify) {
            if (holder.btnOptions != null) {
                holder.btnOptions.setVisibility(View.VISIBLE);
                holder.btnOptions.setOnClickListener(openOptions);
            }
            if (holder.btnGroupOptions != null) {
                holder.btnGroupOptions.setVisibility(View.VISIBLE);
                holder.btnGroupOptions.setOnClickListener(openOptions);
            }
        } else {
            // Không phải bài của mình -> Ẩn nút chỉnh sửa/xóa đi
            if (holder.btnOptions != null) {
                holder.btnOptions.setVisibility(View.GONE);
                holder.btnOptions.setOnClickListener(null);
            }
            if (holder.btnGroupOptions != null) {
                holder.btnGroupOptions.setVisibility(View.GONE);
                holder.btnGroupOptions.setOnClickListener(null);
            }
        }
    }

    private void bindImages(PostViewHolder holder, Post post) {
        List<String> imageUris = post.getImageUris();

        if (imageUris != null && !imageUris.isEmpty()) {
            holder.rvPostImages.setVisibility(View.VISIBLE);
            int imageCount = imageUris.size();

            GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (imageCount == 1) {
                        return 2;
                    }
                    return 1;
                }
            });

            holder.rvPostImages.setLayoutManager(layoutManager);
            holder.rvPostImages.setAdapter(new PostImageAdapter(context, imageUris));

        } else {
            holder.rvPostImages.setVisibility(View.GONE);
        }
    }

    private void showOptionsMenu(View anchor, Post post) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(context, anchor);
        popup.getMenuInflater().inflate(R.menu.post_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                Intent intent = new Intent(context, CreatePostActivity.class);
                intent.putExtra("is_edit_mode", true);
                intent.putExtra("post_id", post.getPostId());
                if (post.getGroupId() != null) intent.putExtra("group_id", post.getGroupId());
                intent.putExtra("post_content", post.getContent());
                intent.putExtra("post_comic_id", post.getComicId());
                intent.putExtra("post_comic_title", post.getComicTitle());
                intent.putExtra("post_hashtag", post.getHashtag());
                intent.putStringArrayListExtra("post_image_urls", post.getImageUris() != null ? new ArrayList<>(post.getImageUris()) : new ArrayList<>());
                context.startActivity(intent);
                return true;
            } else if (id == R.id.action_delete) {
                if (listener != null) listener.onDeletePost(post);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }
}