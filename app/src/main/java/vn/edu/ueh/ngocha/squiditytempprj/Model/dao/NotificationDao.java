package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;

@Dao
public interface NotificationDao {
    @Insert
    void insertNotification(Notification notification);

    @Update
    void updateNotification(Notification notification);

    @Delete
    void deleteNotification(Notification notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    LiveData<List<Notification>> getAllNotifications();

    @Query("SELECT * FROM notifications WHERE is_read = 0 ORDER BY timestamp DESC")
    LiveData<List<Notification>> getUnreadNotifications();

    @Query("UPDATE notifications SET is_read = 1 WHERE notif_id = :notifId")
    void markAsRead(String notifId);
}