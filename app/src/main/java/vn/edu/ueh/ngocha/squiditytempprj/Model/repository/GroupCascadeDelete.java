package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Xoá sạch một group trên Firestore để không còn dữ liệu rác:
 * bình luận của các bài trong group, các bài viết của group, thành viên, rồi cuối cùng mới xoá group.
 * Group được xoá sau cùng nên nếu giữa chừng bị lỗi thì group vẫn còn và có thể xoá lại.
 */
public final class GroupCascadeDelete {

    private static final String TAG = "GroupCascadeDelete";
    // Firestore cho tối đa 500 thao tác mỗi batch, dùng 400 cho an toàn
    private static final int BATCH_SIZE = 400;
    // whereIn chỉ nhận một số id giới hạn mỗi lần truy vấn
    private static final int IN_QUERY_SIZE = 10;

    private GroupCascadeDelete() {
    }

    public interface Callback {
        void onSuccess();

        void onFailure(String message);
    }

    public static void deleteGroup(final FirebaseFirestore db, final String groupId,
                                   @Nullable final Callback callback) {
        if (groupId == null || groupId.isEmpty()) {
            if (callback != null) {
                callback.onFailure("groupId empty");
            }
            return;
        }

        final List<DocumentReference> refs = new ArrayList<>();
        final OnFailureListener failure = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Không xoá được group " + groupId, e);
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }
        };

        // Bước 1: lấy bài viết của group
        db.collection("posts").whereEqualTo("groupId", groupId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot postSnap) {
                        final List<String> postIds = new ArrayList<>();
                        final List<DocumentReference> postRefs = new ArrayList<>();
                        for (DocumentSnapshot p : postSnap.getDocuments()) {
                            postIds.add(p.getId());
                            postRefs.add(p.getReference());
                        }
                        // Bước 2: lấy bình luận của các bài đó
                        collectComments(db, postIds, 0, refs, failure, new Runnable() {
                            @Override
                            public void run() {
                                refs.addAll(postRefs);
                                // Bước 3: lấy thành viên của group
                                collectMembersAndDelete(db, groupId, refs, failure, callback);
                            }
                        });
                    }
                })
                .addOnFailureListener(failure);
    }

    // Lấy bình luận theo từng nhóm id bài viết
    private static void collectComments(final FirebaseFirestore db, final List<String> postIds,
                                        final int start, final List<DocumentReference> refs,
                                        final OnFailureListener failure, final Runnable done) {
        if (start >= postIds.size()) {
            done.run();
            return;
        }
        int end = Math.min(start + IN_QUERY_SIZE, postIds.size());
        db.collection("comments")
                .whereIn("postId", new ArrayList<>(postIds.subList(start, end))).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snap) {
                        for (DocumentSnapshot c : snap.getDocuments()) {
                            refs.add(c.getReference());
                        }
                        collectComments(db, postIds, start + IN_QUERY_SIZE, refs, failure, done);
                    }
                })
                .addOnFailureListener(failure);
    }

    private static void collectMembersAndDelete(final FirebaseFirestore db, final String groupId,
                                                final List<DocumentReference> refs,
                                                final OnFailureListener failure,
                                                @Nullable final Callback callback) {
        db.collection("group_members").whereEqualTo("groupId", groupId).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snap) {
                        for (DocumentSnapshot m : snap.getDocuments()) {
                            refs.add(m.getReference());
                        }
                        // Document group nằm cuối danh sách nên được xoá sau cùng
                        refs.add(db.collection("groups").document(groupId));
                        commitBatches(db, refs, 0, failure, callback);
                    }
                })
                .addOnFailureListener(failure);
    }

    // Xoá lần lượt từng batch, batch sau chỉ chạy khi batch trước thành công
    private static void commitBatches(final FirebaseFirestore db, final List<DocumentReference> refs,
                                      final int start, final OnFailureListener failure,
                                      @Nullable final Callback callback) {
        if (start >= refs.size()) {
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }
        int end = Math.min(start + BATCH_SIZE, refs.size());
        WriteBatch batch = db.batch();
        for (int i = start; i < end; i++) {
            batch.delete(refs.get(i));
        }
        final int next = end;
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        commitBatches(db, refs, next, failure, callback);
                    }
                })
                .addOnFailureListener(failure);
    }
}
