package com.playonline.controllers.services;

import com.google.gson.Gson;
import com.playonline.beans.Playlist;
import com.playonline.beans.User;
import com.playonline.dao.PlaylistDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/user-playlists")
public class UserPlaylistsServlet extends HttpServlet {

    private Connection connection;

    @Override
    public void init() throws ServletException {
        this.connection = MySQLConnections.getConnection(getServletContext());
    }

    //returns the list of all the user's playlists
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            resp.sendRedirect("/PlaylistOnlineJS/login");
            return;
        }
        //getting playlists
        List<Playlist> playlists;
        try {
            playlists = new PlaylistDAO(connection).getUserPlaylists(user.getUsername());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //returning playlists
        Gson gson = new Gson();
        resp.setContentType("application/json");
        resp.getWriter().print(gson.toJson(playlists, playlists.getClass()));
    }
}
