package vn.edu.ueh.ngocha.squiditytempprj;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Admin;

public class AdminRepository {

    private final AdminDao adminDao;

    public AdminRepository(AdminDao adminDao) {
        this.adminDao = adminDao;
    }

    public void insert(Admin admin) {
        adminDao.insert(admin);
    }

    public void update(Admin admin) {
        adminDao.update(admin);
    }

    public void delete(Admin admin) {
        adminDao.delete(admin);
    }

    public List<Admin> getAll() {
        return adminDao.getAll();
    }

    public Admin getById(String userId) {
        return adminDao.getById(userId);
    }

    public void deleteAll() {
        adminDao.deleteAll();
    }
}