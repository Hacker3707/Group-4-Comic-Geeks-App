package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.CommentDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.PostDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;

/**
 * Đồng bộ bài viết và bình luận giữa Firestore và Room để mọi tài khoản/máy đều thấy như nhau.
 *
 * - Firestore: collection "posts" và "comments" (mỗi bình luận là 1 document, có postId của bài).
 * - Room vẫn là nơi giao diện đọc (LiveData), nên chỉ cần chép dữ liệu Firestore về Room là
 *   các màn hình tự cập nhật, không phải sửa giao diện.
 *
 * Ghi: PostRepository / CommentRepository gọi pushComment, deleteCommentRemote...
 * Đọc: start() lắng nghe 2 collection rồi chép về Room.
 *
 * Bài/bình luận chỉ có ở máy (tạo trước khi có đồng bộ) được đẩy lên Firestore ở lần đồng bộ đầu tiên.
 * Số bình luận của mỗi bài (commentCount) được tính lại từ danh sách bình luận thật.
 */
public class PostSyncManager {

    private static final String TAG = "PostSyncManager";
    private static final String POSTS = "posts";
    private static final String COMMENTS = "comments";

    private static PostSyncManager instance;

    public static synchronized PostSyncManager getInstance(Application application) {
        if (instance == null) {
            instance = new PostSyncManager(application);
        }
        return instance;
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final PostDao postDao;
    private final CommentDao commentDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ListenerRegistration postsRegistration;
    private ListenerRegistration commentsRegistration;

    // Các biến dưới đây chỉ được dùng trên executor
    private final Map<String, Post> remotePosts = new HashMap<>();
    private final Map<String, Comment> remoteComments = new HashMap<>();
    private final Set<String> knownRemotePostIds = new HashSet<>();
    private final Set<String> knownRemoteCommentIds = new HashSet<>();
    private boolean postsLoaded = false;
    private boolean commentsLoaded = false;
    private boolean uploadedLocalOnly = false;

    private PostSyncManager(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        postDao = db.postDao();
        commentDao = db.commentDao();
    }

    // =================================
    // Bắt đầu / dừng lắng nghe Firestore
    // =================================

    // Gọi bao nhiêu lần cũng được, chỉ lắng nghe một lần. Chưa đăng nhập thì chưa làm gì.
    public synchronized void start() {
        if (postsRegistration != null) {
            return;
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        postsRegistration = firestore.collection(POSTS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null || snapshots == null) {
                            Log.w(TAG, "Lỗi lắng nghe posts", error);
                            return;
                        }
                        final List<Post> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Post post = toPost(doc);
                            if (post != null) {
                                list.add(post);
                            }
                        }
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                remotePosts.clear();
                                for (Post post : list) {
                                    remotePosts.put(post.getPostId(), post);
                                }
                                postsLoaded = true;
                                reconcile();
                            }
                        });
                    }
                });

        commentsRegistration = firestore.collection(COMMENTS)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null || snapshots == null) {
                            Log.w(TAG, "Lỗi lắng nghe comments", error);
                            return;
                        }
                        final List<Comment> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Comment comment = toComment(doc);
                            if (comment != null) {
                                list.add(comment);
                            }
                        }
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                remoteComments.clear();
                                for (Comment comment : list) {
                                    remoteComments.put(comment.getCommentId(), comment);
                                }
                                commentsLoaded = true;
                                reconcile();
                            }
                        });
                    }
                });
    }

    public synchronized void stop() {
        if (postsRegistration != null) {
            postsRegistration.remove();
            postsRegistration = null;
        }
        if (commentsRegistration != null) {
            commentsRegistration.remove();
            commentsRegistration = null;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                remotePosts.clear();
                remoteComments.clear();
                knownRemotePostIds.clear();
                knownRemoteCommentIds.clear();
                postsLoaded = false;
                commentsLoaded = false;
                uploadedLocalOnly = false;
            }
        });
    }

    // =================================
    // Chép dữ liệu Firestore về Room (chạy trên executor)
    // =================================

    private void reconcile() {
        // Chờ cả 2 collection tải xong lần đầu
        if (!postsLoaded || !commentsLoaded) {
            return;
        }

        // Lần đầu: đẩy lên Firestore những bài/bình luận chỉ có ở máy này
        if (!uploadedLocalOnly) {
            uploadedLocalOnly = true;
            for (Post local : postDao.getAllSync()) {
                if (!remotePosts.containsKey(local.getPostId())) {
                    pushPost(local);
                }
            }
            for (Comment local : commentDao.getAllSync()) {
                if (!remoteComments.containsKey(local.getCommentId())) {
                    pushComment(local);
                }
            }
        }

        // Bài viết: thêm mới hoặc cập nhật nếu có khác biệt
        for (Post remote : remotePosts.values()) {
            Post local = postDao.getByIdSync(remote.getPostId());
            if (local == null) {
                postDao.insertPost(remote);
            } else if (!samePost(local, remote)) {
                postDao.updatePost(remote);
            }
            knownRemotePostIds.add(remote.getPostId());
        }

        // Bài đã bị xoá ở máy khác: xoá khỏi Room (chỉ xoá những bài từng thấy trên Firestore)
        List<String> removedPostIds = new ArrayList<>();
        for (String knownId : knownRemotePostIds) {
            if (!remotePosts.containsKey(knownId)) {
                removedPostIds.add(knownId);
            }
        }
        for (String removedId : removedPostIds) {
            Post local = postDao.getByIdSync(removedId);
            if (local != null) {
                postDao.deletePost(local);
            }
            knownRemotePostIds.remove(removedId);
        }

        // Bình luận: thêm mới hoặc cập nhật
        for (Comment remote : remoteComments.values()) {
            Comment local = commentDao.getByIdSync(remote.getCommentId());
            if (local == null) {
                commentDao.insertComment(remote);
            } else if (!sameComment(local, remote)) {
                commentDao.updateComment(remote);
            }
            knownRemoteCommentIds.add(remote.getCommentId());
        }

        // Bình luận đã bị xoá ở máy khác
        List<String> removedCommentIds = new ArrayList<>();
        for (String knownId : knownRemoteCommentIds) {
            if (!remoteComments.containsKey(knownId)) {
                removedCommentIds.add(knownId);
            }
        }
        for (String removedId : removedCommentIds) {
            Comment local = commentDao.getByIdSync(removedId);
            if (local != null) {
                commentDao.deleteComment(local);
            }
            knownRemoteCommentIds.remove(removedId);
        }

        // Tính lại số bình luận của từng bài từ danh sách bình luận thật
        Map<String, Integer> countByPost = new HashMap<>();
        for (Comment comment : commentDao.getAllSync()) {
            Integer current = countByPost.get(comment.getPostId());
            countByPost.put(comment.getPostId(), current == null ? 1 : current + 1);
        }
        for (Post post : postDao.getAllSync()) {
            Integer count = countByPost.get(post.getPostId());
            int expected = count == null ? 0 : count;
            if (post.getCommentCount() != expected) {
                post.setCommentCount(expected);
                postDao.updatePost(post);
            }
        }
    }

    private boolean samePost(Post a, Post b) {
        return Objects.equals(a.getUserId(), b.getUserId())
                && Objects.equals(a.getGroupId(), b.getGroupId())
                && Objects.equals(a.getComicId(), b.getComicId())
                && Objects.equals(a.getContent(), b.getContent())
                && Objects.equals(a.getTimestamp(), b.getTimestamp())
                && Objects.equals(a.getImageUris(), b.getImageUris())
                && Objects.equals(a.getHashtag(), b.getHashtag())
                && Objects.equals(a.getComicTitle(), b.getComicTitle());
    }

    private boolean sameComment(Comment a, Comment b) {
        return Objects.equals(a.getUserId(), b.getUserId())
                && Objects.equals(a.getPostId(), b.getPostId())
                && Objects.equals(a.getContent(), b.getContent())
                && Objects.equals(a.getTimestamp(), b.getTimestamp());
    }

    // =================================
    // Ghi lên Firestore
    // =================================

    public void pushPost(Post post) {
        if (post == null) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("postId", post.getPostId());
        data.put("userId", post.getUserId());
        data.put("groupId", post.getGroupId());
        data.put("comicId", post.getComicId());
        data.put("content", post.getContent());
        data.put("imageUris", post.getImageUris());
        data.put("timestamp", post.getTimestamp());
        data.put("commentCount", post.getCommentCount());
        data.put("hashtag", post.getHashtag());
        data.put("comicTitle", post.getComicTitle());

        firestore.collection(POSTS).document(post.getPostId())
                .set(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Không ghi được post lên Firestore", e);
                    }
                });
    }

    public void pushComment(Comment comment) {
        if (comment == null) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", comment.getCommentId());
        data.put("userId", comment.getUserId());
        data.put("postId", comment.getPostId());
        data.put("content", comment.getContent());
        data.put("timestamp", comment.getTimestamp());
        if (comment.getParentId() != null) {
            data.put("parentId", comment.getParentId());
            data.put("replyToUsername", comment.getReplyToUsername());
        }

        firestore.collection(COMMENTS).document(comment.getCommentId())
                .set(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Không ghi được comment lên Firestore", e);
                    }
                });
    }

    public void deleteCommentRemote(String commentId) {
        if (commentId == null) {
            return;
        }
        firestore.collection(COMMENTS).document(commentId).delete();
    }

    // Xoá toàn bộ bình luận của một bài viết (khi bài bị xoá)
    public void deleteCommentsOfPostRemote(String postId) {
        if (postId == null) {
            return;
        }
        firestore.collection(COMMENTS).whereEqualTo("postId", postId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            doc.getReference().delete();
                        }
                    }
                });
    }

    // =================================
    // Đổi document Firestore thành entity
    // =================================

    private Post toPost(DocumentSnapshot doc) {
        String id = doc.getId();
        if (id == null || id.isEmpty()) {
            return null;
        }
        List<String> imageUris = null;
        Object rawImages = doc.get("imageUris");
        if (rawImages instanceof List) {
            imageUris = new ArrayList<>();
            for (Object item : (List<?>) rawImages) {
                if (item != null) {
                    imageUris.add(item.toString());
                }
            }
        }
        Post post = new Post(id, doc.getString("userId"), doc.getString("comicId"),
                doc.getString("content"), imageUris);
        post.setGroupId(doc.getString("groupId"));
        post.setHashtag(doc.getString("hashtag"));
        post.setComicTitle(doc.getString("comicTitle"));
        Date timestamp = doc.getDate("timestamp");
        if (timestamp != null) {
            post.setTimestamp(timestamp);
        }
        return post;
    }

    private Comment toComment(DocumentSnapshot doc) {
        String id = doc.getId();
        String postId = doc.getString("postId");
        if (id == null || id.isEmpty() || postId == null) {
            return null;
        }
        Comment comment = new Comment(id, doc.getString("userId"), postId, doc.getString("content"));
        Date timestamp = doc.getDate("timestamp");
        if (timestamp != null) {
            comment.setTimestamp(timestamp);
        }
        comment.setParentId(doc.getString("parentId"));
        comment.setReplyToUsername(doc.getString("replyToUsername"));
        return comment;
    }
}
