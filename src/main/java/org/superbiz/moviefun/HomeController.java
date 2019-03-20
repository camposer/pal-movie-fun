package org.superbiz.moviefun;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final PlatformTransactionManager moviesTransactionManager;
    private final PlatformTransactionManager albumsTransactionManager;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures, PlatformTransactionManager moviesTransactionManager, PlatformTransactionManager albumsTransactionManager) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.moviesTransactionManager = moviesTransactionManager;
        this.albumsTransactionManager = albumsTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {
        setupMovies();
        setupAlbums();


        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }

    private void setupAlbums() {
        TransactionTemplate albumsTransactionTemplate = new TransactionTemplate(albumsTransactionManager);
        albumsTransactionTemplate.execute((TransactionCallback) transactionStatus -> {
            for (Album album : albumFixtures.load()) {
                albumsBean.addAlbum(album);
            }

            return true;
        });
    }

    private void setupMovies() {
        TransactionTemplate moviesTransactionTemplate = new TransactionTemplate(moviesTransactionManager);
        moviesTransactionTemplate.execute((TransactionCallback) transactionStatus -> {
            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }

            return true;
        });
    }
}
