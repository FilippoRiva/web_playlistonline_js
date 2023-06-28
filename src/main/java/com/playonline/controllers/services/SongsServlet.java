package com.playonline.controllers.services;

import com.google.gson.Gson;
import com.playonline.beans.Album;
import com.playonline.beans.Artist;
import com.playonline.beans.Song;
import com.playonline.beans.User;
import com.playonline.dao.AlbumDAO;
import com.playonline.dao.ArtistDAO;
import com.playonline.dao.SongDAO;
import com.playonline.utils.GenreEnum;
import com.playonline.utils.MySQLConnections;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/song")
@MultipartConfig
public class SongsServlet extends HttpServlet {
    private Connection connection;
    private String filesDirectory;

    @Override
    public void init() {
        this.filesDirectory = getServletContext().getInitParameter("filesDirectory");
        this.connection = MySQLConnections.getConnection(getServletContext());
        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //returns the requested song info
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            resp.sendRedirect("login");
            return;
        }
        // input parsing
        String id_str = req.getParameter("id");
        int id;
        try {
            id = Integer.parseInt(id_str);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid Playlist ID");
            return;
        }

        // page templating
        Song song;
        try {
            song = new SongDAO(this.connection, filesDirectory).getSongByID(id, user.getUsername());
            this.connection.commit(); //seeing other transactions' changes
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error");
            return;
        }
        if (song == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid username-song couple");
            return;
        }
        Gson gson = new Gson();
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(song, song.getClass()));
    }

    //adds a song to the user's songs
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //auth check
        User user = (User) req.getSession().getAttribute("user");
        if (req.getSession().getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            resp.sendRedirect("login");
            return;
        }
        //input parsing
        Part song = req.getPart("s_file");
        Part cover = req.getPart("a_cover");
        String s_title = req.getParameter("s_title");
        String s_genre = req.getParameter("s_genre");
        String a_name = req.getParameter("a_name");
        String a_ID_str = req.getParameter("a_ID");
        String ar_name = req.getParameter("ar_name");
        String a_date_str = req.getParameter("a_date");
        String existing_str = req.getParameter("existing");
        boolean existing;
        existing = Boolean.parseBoolean(existing_str);
        if (!existing && (song.getSize() == 0 || s_title == null || s_title.equals("") ||
                a_name == null || a_name.equals("") || ar_name == null || ar_name.equals("") ||
                a_date_str.equals("") || cover.getSize() == 0 || s_genre == null || s_genre.equals("none"))) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("fill every field");
            return;
        }
        if (existing && (song.getSize() == 0 || s_title == null || s_title.equals("") || s_genre == null || s_genre.equals("none"))) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("fill every field");
            return;
        }
        int a_ID = -1;
        Date a_date = null;
        if (!existing) {
            try {
                a_date = new SimpleDateFormat("yyyy-dd-MM").parse(a_date_str);
            } catch (ParseException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("date is not a date");
                return;
            }
        } else {
            if (a_ID_str == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("no album set");
                return;
            }
            try {
                a_ID = Integer.parseInt(a_ID_str);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("albumID is not an ID");
                return;
            }
        }
        GenreEnum genre;
        try {
            genre = GenreEnum.valueOf(s_genre.toUpperCase());
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("invalid genre");
            return;
        }
        if (!song.getContentType().startsWith("audio/") || (cover != null && cover.getSize() != 0 && !cover.getContentType().startsWith("image/"))) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("invalid file format");
            return;
        }

        // db update
        Album album = null;
        Artist artist;
        ArtistDAO artistDAO = new ArtistDAO(connection);
        AlbumDAO albumDAO = new AlbumDAO(connection, filesDirectory);
        SongDAO songDAO = new SongDAO(connection, filesDirectory);
        try {
            if (!existing) {
                artist = artistDAO.addArtist(user.getUsername(), ar_name);
                album = albumDAO.addAlbum(a_name, artist, a_date, cover, user.getUsername());
            } else {
                album = albumDAO.getAlbum(user.getUsername(), a_ID);
            }
            songDAO.addSong(user.getUsername(), s_title, genre, song, album);
            connection.commit();
        } catch (InvalidParameterException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("album doesn't exists");
            return;
        } catch (SQLException e) {
            try {
                connection.rollback();
                //cover deletion when song query or song file save fails
                albumDAO.deleteAlbumCover(user.getUsername(), album);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error");
            return;
        }
        // success
        resp.setContentType("text/plain");
        resp.getWriter().write(Integer.toString(album.getId()));
    }
}
