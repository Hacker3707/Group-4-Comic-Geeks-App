package vn.edu.ueh.ngocha.squiditytempprj.View.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.R;

/**
 * Tải ảnh bìa / ảnh đại diện của nhóm (chọn từ thư viện ảnh) lên Firebase Storage
 * và trả về link tải xuống để lưu vào Group.
 * - Ảnh được thu nhỏ và nén JPEG trước khi tải để nhanh và nhẹ.
 * - Quá thời gian chờ (mất mạng...) thì huỷ và báo lỗi, không để màn hình treo.
 * Mọi callback đều chạy trên luồng chính.
 */
public final class GroupImageUploader {

    private static final int COVER_MAX_SIDE = 1280;
    private static final int AVATAR_MAX_SIDE = 512;
    private static final int JPEG_QUALITY = 85;
    private static final long UPLOAD_TIMEOUT_MS = 30000L;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    // Kết quả cuối cùng của cả 2 ảnh
    public interface ResultListener {
        void onDone(String coverUrl, String avatarUrl);

        void onError(int messageResId);
    }

    // Kết quả của 1 ảnh (url = null nghĩa là không có ảnh mới cần tải)
    private interface SingleListener {
        void onSuccess(String url);

        void onError(int messageResId);
    }

    private GroupImageUploader() {
    }

    /**
     * coverUri / avatarUri: ảnh mới vừa chọn từ thư viện (null nếu không đổi).
     * currentCoverUrl / currentAvatarUrl: link đang có (dùng lại khi không chọn ảnh mới,
     * truyền chuỗi rỗng nếu muốn xoá ảnh).
     */
    public static void uploadImages(Context context, final Uri coverUri, final Uri avatarUri,
                                    String currentCoverUrl, String currentAvatarUrl,
                                    final ResultListener listener) {
        final Context appContext = context.getApplicationContext();
        final String keepCover = currentCoverUrl == null ? "" : currentCoverUrl;
        final String keepAvatar = currentAvatarUrl == null ? "" : currentAvatarUrl;

        uploadOne(appContext, "cover", coverUri, COVER_MAX_SIDE, new SingleListener() {
            @Override
            public void onSuccess(String newCoverUrl) {
                final String finalCover = newCoverUrl != null ? newCoverUrl : keepCover;

                uploadOne(appContext, "avatar", avatarUri, AVATAR_MAX_SIDE, new SingleListener() {
                    @Override
                    public void onSuccess(String newAvatarUrl) {
                        String finalAvatar = newAvatarUrl != null ? newAvatarUrl : keepAvatar;
                        listener.onDone(finalCover, finalAvatar);
                    }

                    @Override
                    public void onError(int messageResId) {
                        listener.onError(messageResId);
                    }
                });
            }

            @Override
            public void onError(int messageResId) {
                listener.onError(messageResId);
            }
        });
    }

    private static void uploadOne(final Context appContext, final String kind, final Uri uri,
                                  final int maxSide, final SingleListener listener) {
        if (uri == null) {
            listener.onSuccess(null);
            return;
        }

        // Đọc + thu nhỏ + nén ảnh ở luồng nền
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                byte[] bytes = null;
                try {
                    bytes = compress(appContext, uri, maxSide);
                } catch (Exception e) {
                    bytes = null;
                }
                final byte[] data = bytes;

                MAIN.post(new Runnable() {
                    @Override
                    public void run() {
                        if (data == null) {
                            listener.onError(R.string.group_image_read_failed);
                        } else {
                            uploadBytes(kind, data, listener);
                        }
                    }
                });
            }
        });
    }

    private static byte[] compress(Context context, Uri uri, final int maxSide) throws IOException {
        ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
        Bitmap bitmap = ImageDecoder.decodeBitmap(source, new ImageDecoder.OnHeaderDecodedListener() {
            @Override
            public void onHeaderDecoded(@NonNull ImageDecoder decoder,
                                        @NonNull ImageDecoder.ImageInfo info,
                                        @NonNull ImageDecoder.Source src) {
                int width = info.getSize().getWidth();
                int height = info.getSize().getHeight();
                int longest = Math.max(width, height);
                if (longest > maxSide) {
                    float scale = (float) maxSide / longest;
                    decoder.setTargetSize(
                            Math.max(1, Math.round(width * scale)),
                            Math.max(1, Math.round(height * scale)));
                }
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
            }
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        bitmap.recycle();
        return out.toByteArray();
    }

    private static void uploadBytes(String kind, byte[] data, final SingleListener listener) {
        final StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("group_images/" + kind + "/" + UUID.randomUUID() + ".jpg");
        final UploadTask task = ref.putBytes(data);

        // Mất mạng thì Storage cứ thử lại rất lâu, nên tự huỷ sau một lúc
        final Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if (!task.isComplete()) {
                    task.cancel();
                }
            }
        };
        MAIN.postDelayed(timeout, UPLOAD_TIMEOUT_MS);

        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot snapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        MAIN.removeCallbacks(timeout);
                        listener.onSuccess(downloadUri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MAIN.removeCallbacks(timeout);
                        listener.onError(R.string.group_image_upload_failed);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MAIN.removeCallbacks(timeout);
                listener.onError(R.string.group_image_upload_failed);
            }
        });
    }
}
