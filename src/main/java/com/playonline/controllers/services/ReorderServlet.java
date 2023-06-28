package com.playonline.controllers.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.playonline.beans.Song;
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
import java.util.HashSet;
import java.util.List;

@WebServlet("/reorder")
public class ReorderServlet extends HttpServlet {

    private Connection connection;

    @Override
    public void init() throws ServletException {
        try {
            this.connection = MySQLConnections.getConnection(getServletContext());
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            resp.sendRedirect("login");
            return;
        }

        String json = req.getReader().readLine();
        String id_str = req.getParameter("id");
        Gson gson = new Gson();
        int id;
        List<Integer> orderedList;
        try {
            List<Song> songList = gson.fromJson(json, new TypeToken<List<Song>>() {
            }.getType());
            orderedList = songList.stream().map(Song::getId).toList();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Corrupted Song List");
            return;
        }
        try {
            id = Integer.parseInt(id_str);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid playlist ID");
            return;
        }
        List<Integer> actualIds;
        PlaylistDAO playlistDAO = new PlaylistDAO(this.connection);
        try {
            actualIds = playlistDAO.getPlaylistIds(id, user.getUsername());
            if (actualIds == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid playlist-user couple");
                return;
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error");
            return;
        }
        //checking orderedList content
        if (orderedList.size() != actualIds.size()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("IDs are not valid");
            return;
        }
        HashSet<Integer> map = new HashSet<>(actualIds);
        for (int songId : orderedList) {
            if (!map.contains(songId)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("ID is not valid : " + songId);
                return;
            }
        }

        try {
            for (int i = 0; i < orderedList.size(); i++) {
                playlistDAO.updatePosition(id, orderedList.get(i), i);
            }
            this.connection.commit();
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error");
        }
    }
}
