package vn.edu.ueh.ngocha.squiditytempprj;

import java.util.List;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Creator;

public class CreatorRepository {

    private final CreatorDao creatorDao;

    public CreatorRepository(CreatorDao creatorDao) {
        this.creatorDao = creatorDao;
    }

    public void insert(Creator creator) {
        creatorDao.insert(creator);
    }

    public void update(Creator creator) {
        creatorDao.update(creator);
    }

    public void delete(Creator creator) {
        creatorDao.delete(creator);
    }

    public List<Creator> getAll() {
        return creatorDao.getAll();
    }

    public Creator getById(String userId) {
        return creatorDao.getById(userId);
    }

    public void deleteAll() {
        creatorDao.deleteAll();
    }
}