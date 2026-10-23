package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.edu.ueh.ngocha.squiditytempprj.Model.AppDatabase;
import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.UserDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Review;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

public class UserRepository {

    private final UserDao userDao;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private final LiveData<List<User>> allUsers;
    private final LiveData<List<User>> normalUsers;
    private final LiveData<List<User>> admins;
    private final LiveData<List<User>> creators;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public UserRepository(Application application) {

        AppDatabase database =
                AppDatabase.getDatabase(application);

        userDao = database.userDao();

        allUsers = userDao.getAll();
        normalUsers = userDao.getNormalUsers();
        admins = userDao.getAdmins();
        creators = userDao.getCreators();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<List<User>> getUsersByIds(List<String> userIds) {
        return new LiveData<List<User>>() {

            private ListenerRegistration userListener;

            @Override
            protected void onActive() {
                super.onActive();

                if (userListener == null) {
                    if (userIds == null || userIds.isEmpty()) {
                        postValue(new ArrayList<User>());
                        return;
                    }

                    userListener = firestore.collection("users")
                            .whereIn("userId", userIds)
                            .addSnapshotListener(
                                    new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(
                                                @Nullable QuerySnapshot snapshots,
                                                @Nullable FirebaseFirestoreException error) {

                                            if (error != null) {
                                                Log.e("UserRepository",
                                                        "Error getting users", error);
                                                return;
                                            }

                                            if (snapshots == null) {
                                                return;
                                            }

                                            List<User> users = new ArrayList<>();

                                            for (QueryDocumentSnapshot document : snapshots) {
                                                User user = document.toObject(User.class);
                                                users.add(user);
                                            }

                                            postValue(users);
                                        }
                                    }
                            );
                }
            }

            @Override
            protected void onInactive() {
                super.onInactive();

                if (userListener != null) {
                    userListener.remove();
                    userListener = null;
                }
            }
        };
    }


    // Theo dõi THỜI GIAN THỰC tên + ảnh của nhiều tài khoản (key của Map = userId).
    // Document id của collection "users" chính là uid. Firestore chỉ cho whereIn tối đa
    // 10 giá trị nên chia danh sách thành từng nhóm 10. Listener tự gỡ khi không còn ai quan sát.
    public LiveData<Map<String, User>> watchUsersByIds(final List<String> userIds) {
        return new LiveData<Map<String, User>>() {

            private final List<ListenerRegistration> registrations = new ArrayList<>();
            private final Map<String, User> merged = new HashMap<>();

            @Override
            protected void onActive() {
                super.onActive();

                if (!registrations.isEmpty()) {
                    return;
                }
                if (userIds == null || userIds.isEmpty()) {
                    setValue(new HashMap<String, User>());
                    return;
                }

                int chunkSize = 10;
                for (int start = 0; start < userIds.size(); start += chunkSize) {
                    int end = Math.min(start + chunkSize, userIds.size());
                    final List<String> chunk = new ArrayList<>(userIds.subList(start, end));

                    ListenerRegistration registration = firestore.collection("users")
                            .whereIn(FieldPath.documentId(), chunk)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot snapshots,
                                                    @Nullable FirebaseFirestoreException error) {
                                    if (error != null) {
                                        Log.e("UserRepository", "Error watching users", error);
                                        return;
                                    }
                                    if (snapshots == null) {
                                        return;
                                    }

                                    for (String id : chunk) {
                                        merged.remove(id);
                                    }
                                    for (QueryDocumentSnapshot document : snapshots) {
                                        User user = new User(
                                                document.getId(),
                                                document.getString("username"),
                                                document.getString("email"),
                                                "",
                                                document.getString("avatarUrl"));
                                        merged.put(document.getId(), user);
                                    }
                                    setValue(new HashMap<String, User>(merged));
                                }
                            });
                    registrations.add(registration);
                }
            }

            @Override
            protected void onInactive() {
                super.onInactive();

                for (ListenerRegistration registration : registrations) {
                    registration.remove();
                }
                registrations.clear();
            }
        };
    }

    public LiveData<List<User>> getNormalUsersLive() {
        return normalUsers;
    }

    public LiveData<List<User>> getAdminsLive() {
        return admins;
    }

    public LiveData<List<User>> getCreatorsLive() {
        return creators;
    }

    // Thêm tài khoản
    public void insert(final User user) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                userDao.insert(user);
            }
        });
    }

    // Cập nhật tài khoản
    public void update(final User user) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                userDao.update(user);
            }
        });
    }

    // Xóa một tài khoản
    public void delete(final User user) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                userDao.delete(user);
            }
        });
    }

    // Xóa toàn bộ tài khoản
    public void deleteAll() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                userDao.deleteAll();
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}