package vn.edu.ueh.ngocha.squiditytempprj.View.notification;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ChatActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.MainActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.ProfileActivity;
import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;

/**
 * Bấm vào một thông báo thì mở đúng nơi liên quan:
 * MESSAGE -> khung chat với người nhắn, FOLLOW -> trang cá nhân người follow,
 * LIKE/COMMENT -> bài viết trên trang chủ.
 */
public final class NotificationNavigator {

    private NotificationNavigator() {}

    public static void open(Activity activity, Notification n) {
        if (n == null || n.getType() == null) return;
        String actorId = n.getActorId();
        switch (n.getType()) {
            case MESSAGE: {
                FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
                if (me == null || actorId == null || actorId.isEmpty()) return;
                String myId = me.getUid();
                String roomId = myId.compareTo(actorId) <= 0 ? myId + "_" + actorId : actorId + "_" + myId;
                String title = n.getActorName() != null && !n.getActorName().isEmpty()
                        ? n.getActorName() : Str.t(R.string.common_user);
                Intent i = new Intent(activity, ChatActivity.class);
                i.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, roomId);
                i.putExtra(ChatActivity.EXTRA_CURRENT_USER_ID, myId);
                i.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, actorId);
                i.putExtra(ChatActivity.EXTRA_CHAT_TITLE, title);
                activity.startActivity(i);
                break;
            }
            case FOLLOW: {
                if (actorId == null || actorId.isEmpty()) return;
                Intent i = new Intent(activity, ProfileActivity.class);
                i.putExtra(ProfileActivity.EXTRA_USER_ID, actorId);
                activity.startActivity(i);
                break;
            }
            case LIKE:
            case COMMENT: {
                Intent i = new Intent(activity, MainActivity.class);
                i.putExtra(MainActivity.EXTRA_POST_ID, n.getPostId());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(i);
                break;
            }
            default:
                break;
        }
    }
}
