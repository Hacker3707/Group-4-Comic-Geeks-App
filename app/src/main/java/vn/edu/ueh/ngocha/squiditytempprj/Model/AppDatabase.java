package vn.edu.ueh.ngocha.squiditytempprj.Model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ChatRoomDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ComicDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.CommentDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.FollowDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GenreDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GroupDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GroupMemberDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.PostDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.MessageDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.NotificationDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ReviewDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.StampDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.UserDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.ChatRoom;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comment;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Creator;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Group;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.GroupMember;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Follow;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Message;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Post;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Stamp;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

@Database(entities = {User.class, Post.class, Comment.class, Stamp.class,
        Review.class, Comic.class, Genre.class, Creator.class, Follow.class, Group.class,
        GroupMember.class, Message.class, ChatRoom.class, Notification.class}, version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ComicDao comicDao();
    public abstract GenreDao genreDao();
    public abstract ReviewDao reviewDao();
    public abstract UserDao userDao();
    public abstract PostDao postDao();
    public abstract CommentDao commentDao();
    public abstract StampDao stampDao();
    public abstract GroupDao groupDao();
    public abstract GroupMemberDao groupMemberDao();
    public abstract FollowDao followDao();


    public abstract NotificationDao notificationDao();
    public abstract MessageDao messageDao();
    public abstract ChatRoomDao chatRoomDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "squidity_database")
                            .addCallback(oncreateCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback oncreateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            initializeData();
        }
    };

    private static void initializeData() {

    }
}
