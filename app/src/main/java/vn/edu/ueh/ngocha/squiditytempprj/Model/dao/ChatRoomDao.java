package vn.edu.ueh.ngocha.squiditytempprj.Model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;

@Dao
public interface ChatRoomDao {
    @Insert
    void insertChatRoom(ChatRoom chatRoom);

    @Delete
    void deleteChatRoom(ChatRoom chatRoom);

    @Query("SELECT * FROM chat_rooms WHERE user1_id = :userId OR user2_id = :userId")
    LiveData<List<ChatRoom>> getChatRoomsByUserId(String userId);

    @Query("SELECT * FROM chat_rooms WHERE chat_room_id = :chatRoomId")
    LiveData<ChatRoom> getChatRoomById(String chatRoomId);
}