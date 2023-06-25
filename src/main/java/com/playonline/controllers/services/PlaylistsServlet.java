package com.playonline.controllers.services;

import com.google.gson.Gson;
import com.playonline.beans.Playlist;
import com.playonline.beans.User;
import com.playonline.dao.PlaylistDAO;
import com.playonline.dao.SongDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/playlist")
@MultipartConfig
public class PlaylistsServlet extends HttpServlet {
    Connection connection;
    @Override
    public void init(){
        this.connection = MySQLConnections.getConnection(getServletContext());
    }

    //returns the requested playlist's songs infos
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect("login");
            return;
        }

        // input parsing
        String p_ID_str = req.getParameter("id");
        int p_ID;
        try {
            p_ID = Integer.parseInt(p_ID_str);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("p_ID is not a number");
            return;
        }

        // database query
        Playlist playlist;
        try {
            playlist = new PlaylistDAO(connection).getPlaylist(p_ID, user.getUsername());
            if(playlist==null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        Gson gson = new Gson();
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(playlist,playlist.getClass()));
    }

    // add songs to a playlist
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if(user == null){
            resp.sendRedirect("login");
            return;
        }
        // input parsing
        Map<String,String[]> parameters = req.getParameterMap();
        String playlistID = req.getParameter("p_ID");
        int p_ID;
        try{
            p_ID = Integer.parseInt(playlistID);
        } catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if(!parameters.containsKey("s_ID")){
            return;
        }
        // database update
        String[] songIDs = parameters.get("s_ID");
        try {
            PlaylistDAO playlistDAO = new PlaylistDAO(connection);
            SongDAO songDAO = new SongDAO(connection,"");
            for (String s_ID : songIDs) {
                try {
                    int id = Integer.parseInt(s_ID);
                    if(songDAO.isOwner(user.getUsername(),id))playlistDAO.addSongToPlaylist(user.getUsername(),p_ID, id);
                } catch (NumberFormatException ignored) { //ignoring non parsable IDs
                }
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
