package vn.edu.ueh.ngocha.squiditytempprj.Model.repository;

import vn.edu.ueh.ngocha.squiditytempprj.Model.dao.GenreDao;
import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Genre;

public class GenreRepository {
    public final GenreDao genreDao;

    public GenreRepository(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    public void insert(Genre genre) {
        this.genreDao.insertGenre(genre);
    }

    public void getAll() {
        this.genreDao.getAllGenres();
    }

    public void deleteAll() {
        this.genreDao.deleteAll();
    }


}
