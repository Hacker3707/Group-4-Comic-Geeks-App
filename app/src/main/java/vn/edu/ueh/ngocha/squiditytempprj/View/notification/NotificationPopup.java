package vn.edu.ueh.ngocha.squiditytempprj.View.notification;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.NotificationType;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.adapter.NotificationAdapter;
import vn.edu.ueh.ngocha.squiditytempprj.ViewModel.NotificationViewModel;

/**
 * Popup thông báo ngay trong app (không tạo Activity mới), mở khi bấm nút chuông.
 * Đồng thời theo dõi thông báo chưa đọc để hiện số trên chuông và đẩy thông báo mới ra thanh thông báo hệ thống.
 *
 * Cách dùng trong onCreate của Activity:
 *   NtfChannel.setup(this);
 *   NotificationPopup.attach(this, findViewById(R.id.btnNotification));
 */
public class NotificationPopup {

    // Chỉ để chạy thử: nhấn giữ nút chuông sẽ tạo một thông báo mẫu. Đặt false khi nhóm nối sự kiện thật
    private static final boolean DEMO_MODE = true;

    private static final int POPUP_WIDTH_DP = 320;

    private final AppCompatActivity activity;
    private final View bellButton;
    private final NotificationViewModel viewModel;
    private final NotificationAdapter adapter;
    private final PopupWindow popupWindow;
    private final RecyclerView rcvNotifications;
    private final TextView tvEmpty;
    private final TextView tvBadge;

    private List<Notification> allNotifications = new ArrayList<>();

    // Các thông báo đã xử lý, tránh đẩy ra hệ thống lặp lại
    private final Set<String> handledIds = new HashSet<>();
    private boolean firstUnreadLoad = true;

    public static NotificationPopup attach(AppCompatActivity activity, View bellButton) {
        return new NotificationPopup(activity, bellButton);
    }

    private NotificationPopup(AppCompatActivity activity, View bellButton) {
        this.activity = activity;
        this.bellButton = bellButton;
        this.viewModel = new ViewModelProvider(activity).get(NotificationViewModel.class);
        // Bắt đầu nhận thông báo từ Firestore
        viewModel.startListening();

        // Chấm đỏ đếm thông báo chưa đọc (có thể null nếu màn hình không có thanh trên)
        tvBadge = activity.findViewById(R.id.tvNotificationBadge);

        View content = LayoutInflater.from(activity).inflate(R.layout.popup_notification, null);
        rcvNotifications = content.findViewById(R.id.rcvNotifications);
        tvEmpty = content.findViewById(R.id.tvNotificationEmpty);
        TextView btnMarkAllRead = content.findViewById(R.id.btnMarkAllRead);

        adapter = new NotificationAdapter(new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(Notification notification) {
                if (!notification.isRead()) {
                    viewModel.markNotificationAsRead(notification.getNotifId());
                }
            }
        });
        rcvNotifications.setLayoutManager(new LinearLayoutManager(activity));
        rcvNotifications.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAllAsRead();
            }
        });

        popupWindow = new PopupWindow(content, dp(POPUP_WIDTH_DP), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(dp(8));
        // Cần một background (trong suốt) để chạm ra ngoài thì popup tự đóng
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        bellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        if (DEMO_MODE) {
            bellButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    viewModel.insertNotification(new Notification(UUID.randomUUID().toString(),
                            "Meilin Zhang vừa theo dõi bạn", NotificationType.FOLLOW));
                    return true;
                }
            });
        }

        observeData();
    }

    // Observer đặt trong lớp gắn với Activity: ViewModel cấp LiveData, giao diện tự cập nhật
    private void observeData() {
        viewModel.getAllNotifications().observe(activity, new Observer<List<Notification>>() {
            @Override
            public void onChanged(List<Notification> notifications) {
                allNotifications = (notifications != null) ? notifications : new ArrayList<Notification>();
                adapter.setNotifications(allNotifications);
                boolean isEmpty = allNotifications.isEmpty();
                tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                rcvNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getUnreadNotifications().observe(activity, new Observer<List<Notification>>() {
            @Override
            public void onChanged(List<Notification> unread) {
                updateBadge(unread);
                pushNewOnesToSystem(unread);
            }
        });
    }

    // Hiện số thông báo chưa đọc trên chuông, từ 10 trở lên hiện "9+"
    private void updateBadge(List<Notification> unread) {
        if (tvBadge == null) {
            return;
        }
        int count = (unread != null) ? unread.size() : 0;
        if (count == 0) {
            tvBadge.setVisibility(View.GONE);
        } else {
            tvBadge.setText(count > 9 ? "9+" : String.valueOf(count));
            tvBadge.setVisibility(View.VISIBLE);
        }
    }

    // Lần tải đầu chỉ ghi nhận thông báo cũ, các lần sau có thông báo mới mới đẩy ra hệ thống
    private void pushNewOnesToSystem(List<Notification> unread) {
        if (unread == null) {
            return;
        }
        for (Notification notification : unread) {
            if (handledIds.add(notification.getNotifId()) && !firstUnreadLoad && isRecent(notification)) {
                NtfChannel.show(activity, notification);
            }
        }
        firstUnreadLoad = false;
    }

    // Chỉ đẩy ra hệ thống thông báo vừa xảy ra (dưới 2 phút), tránh hiện hàng loạt thông báo cũ khi mới cài app
    private boolean isRecent(Notification notification) {
        if (notification.getTimestamp() == null) {
            return false;
        }
        return System.currentTimeMillis() - notification.getTimestamp().getTime() < 2 * 60 * 1000;
    }

    private void markAllAsRead() {
        for (Notification notification : allNotifications) {
            if (!notification.isRead()) {
                viewModel.markNotificationAsRead(notification.getNotifId());
            }
        }
    }

    private void toggle() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
            return;
        }
        // Căn mép phải popup với mép phải nút chuông
        int xOffset = bellButton.getWidth() - dp(POPUP_WIDTH_DP);
        popupWindow.showAsDropDown(bellButton, xOffset, dp(4));
    }

    private int dp(int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}