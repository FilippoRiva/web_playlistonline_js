package com.playonline.controllers.services;

import com.google.gson.Gson;
import com.playonline.beans.Album;
import com.playonline.beans.User;
import com.playonline.dao.AlbumDAO;
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

@WebServlet("/albums")
public class AlbumsServlet extends HttpServlet {
    private Connection connection;
    @Override
    public void init() throws ServletException {
        this.connection = MySQLConnections.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User)req.getSession().getAttribute("user");
        if(user==null){
            resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            resp.sendRedirect("login");
            return;
        }
        List<Album> albums;
        try {
            albums = new AlbumDAO(connection,"").getUserAlbums(user.getUsername());
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Internal server error");
            return;
        }
        Gson gson = new Gson();
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(albums,albums.getClass()));
    }
}
