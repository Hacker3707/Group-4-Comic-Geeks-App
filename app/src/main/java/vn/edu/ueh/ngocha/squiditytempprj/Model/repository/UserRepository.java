package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.UserDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.User;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void update(User user) {
        userDao.update(user);
    }

    public void delete(User user) {
        userDao.delete(user);
    }

    public List<User> getAll() {
        return userDao.getAll();
    }

    public User getById(String userId) {
        return userDao.getById(userId);
    }

    public List<User> getNormalUsers() {
        return userDao.getNormalUsers();
    }

    public List<User> getAdmins() {
        return userDao.getAdmins();
    }

    public List<User> getCreators() {
        return userDao.getCreators();
    }

    public void deleteAll() {
        userDao.deleteAll();
    }
}