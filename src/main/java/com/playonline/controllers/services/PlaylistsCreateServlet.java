package com.playonline.controllers.services;

import com.playonline.beans.User;
import com.playonline.dao.PlaylistDAO;
import com.playonline.dao.SongDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/playlist-create")
@MultipartConfig
public class PlaylistsCreateServlet extends HttpServlet {
    Connection connection;
    RequestDispatcher homeDispatcher;
    @Override
    public void init(){
        homeDispatcher = getServletContext().getRequestDispatcher("/home");
        this.connection = MySQLConnections.getConnection(getServletContext());
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // auth check
        User user = (User)req.getSession().getAttribute("user");
        if(user==null){
            resp.sendRedirect("login");
            return;
        }
        // input parsing
        String title = req.getParameter("p_title");
        String username = user.getUsername();
        if(title==null || title.equals("")){
            homeDispatcher.forward(req,resp);
            return;
        }
        // database update
        Map<String,String[]> parameters = req.getParameterMap();
        String[] songIDs = parameters.get("s_ID");
        try {
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);
            SongDAO songDAO = new SongDAO(connection,"");
            int p_ID = playlistDAO.createPlaylist(username, title);
            if (songIDs != null) {
                for (String s_ID : songIDs) {
                    try {
                        int id = Integer.parseInt(s_ID);
                        if(songDAO.isOwner(user.getUsername(),id)) playlistDAO.addSongToPlaylist(p_ID, id);
                    } catch (NumberFormatException ignored) { // ignoring non parsable IDs
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
