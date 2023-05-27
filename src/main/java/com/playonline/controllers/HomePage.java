package com.playonline.controllers;

import com.playonline.beans.User;
import com.playonline.dao.AlbumDAO;
import com.playonline.dao.PlaylistDAO;
import com.playonline.dao.SongDAO;
import com.playonline.utils.Htmls;
import com.playonline.utils.MySQLConnections;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


@WebServlet("/home")
@MultipartConfig
public class HomePage extends HttpServlet {
    PlaylistDAO playlistDAO;
    AlbumDAO albumDAO;
    SongDAO songDAO;

    @Override
    public void init() {
        try{
            Connection connection = MySQLConnections.getConnection(getServletContext());
            String filesDirectory = getServletContext().getInitParameter("filesDirectory");
            this.playlistDAO = new PlaylistDAO(connection);
            this.albumDAO = new AlbumDAO(connection, filesDirectory);
            this.songDAO = new SongDAO(connection, filesDirectory);
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if(user == null) {
            resp.sendRedirect("/PlaylistOnlineJS/login");
            return;
        }

        resp.getWriter().print(Htmls.getInstance(getServletContext()).homePage);
    }
}
