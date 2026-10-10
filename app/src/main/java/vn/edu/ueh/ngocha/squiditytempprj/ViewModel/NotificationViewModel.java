package vn.edu.ueh.ngocha.squiditytempprj.ViewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.ChatRoomRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.MessageRepository;
import vn.edu.ueh.ngocha.squiditytempprj.Model.repository.NotificationRepository;

public class NotificationViewModel extends AndroidViewModel {
    private NotificationRepository notificationRepository;
    private MessageRepository messageRepository;
    private ChatRoomRepository chatRoomRepository;

    private LiveData<List<Notification>> allNotifications;
    private LiveData<List<Notification>> unreadNotifications;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = new NotificationRepository(application);
        messageRepository = new MessageRepository(application);
        chatRoomRepository = new ChatRoomRepository(application);
        allNotifications = notificationRepository.getAllNotifications();
        unreadNotifications = notificationRepository.getUnreadNotifications();
    }

    // --- NOTIFICATION ---
    public LiveData<List<Notification>> getAllNotifications() {
        return allNotifications;
    }

    public LiveData<List<Notification>> getUnreadNotifications() {
        return unreadNotifications;
    }

    public void insertNotification(Notification notification) {
        notificationRepository.insert(notification);
    }

    public void markNotificationAsRead(String notifId) {
        notificationRepository.markAsRead(notifId);
    }

    public void deleteNotification(Notification notification) {
        notificationRepository.delete(notification);
    }

}