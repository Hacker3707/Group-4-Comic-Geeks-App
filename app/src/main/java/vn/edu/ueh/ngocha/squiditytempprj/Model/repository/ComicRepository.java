package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.ComicDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Comic;

public class ComicRepository {
    private final ComicDao comicDao;

    public ComicRepository(ComicDao comicDao) {
        this.comicDao = comicDao;
    }

    public void insert(Comic comic) {
        comicDao.insertComic(comic);
    }

    public void getAll() {
        comicDao.getAllComics();
    }

    public void deleteAll() {
        comicDao.deleteAll();
    }

    public void delete(Comic comic) { comicDao.deleteComic(comic); }
}
