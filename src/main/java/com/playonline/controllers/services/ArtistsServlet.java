package com.playonline.controllers.services;

import com.google.gson.Gson;
import com.playonline.beans.Artist;
import com.playonline.beans.User;
import com.playonline.dao.PlaylistDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/artists")
public class ArtistsServlet extends HttpServlet {
    PlaylistDAO playlistDAO;
    @Override
    public void init() throws ServletException {
        try {
            playlistDAO = new PlaylistDAO(MySQLConnections.getConnection(getServletContext()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if(user==null){
            resp.sendRedirect("/PlaylistOnlineJS/login");
            return;
        }
        List<Artist> artists;
        try {
            artists = playlistDAO.getArtists(user.getUsername());
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        Gson gson = new Gson();
        resp.setContentType("application/json");
        String response = gson.toJson(artists);
        resp.getWriter().write(response);
    }
}
