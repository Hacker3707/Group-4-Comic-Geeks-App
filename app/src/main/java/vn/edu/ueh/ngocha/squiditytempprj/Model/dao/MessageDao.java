package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;

@Dao
public interface MessageDao {
    @Insert
    void insertMessage(Message message);

    @Delete
    void deleteMessage(Message message);

    @Query("SELECT * FROM messages WHERE chat_room_id = :chatRoomId ORDER BY timestamp ASC")
    LiveData<List<Message>> getMessagesByChatRoomId(String chatRoomId);

    @Query("UPDATE messages SET is_read = 1 WHERE message_id = :messageId")
    void markAsRead(String messageId);

    @Query("DELETE FROM messages WHERE chat_room_id = :chatRoomId")
    void deleteMessagesByChatRoomId(String chatRoomId);
}