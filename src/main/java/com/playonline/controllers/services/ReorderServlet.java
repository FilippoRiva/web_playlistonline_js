package com.playonline.controllers.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.playonline.beans.Song;
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
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String json = req.getReader().readLine();
        String id_str = req.getParameter("id");
        Gson gson = new Gson();
        int id;
        List<Integer> orderedList;
        try{
            List<Song> songList = gson.fromJson(json,new TypeToken<List<Song>>() {}.getType());
            orderedList = songList.stream().map(Song::getId).toList();
        } catch (Exception e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try{
            id = Integer.parseInt(id_str);
        } catch(NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        List<Integer> actualIds;
        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
        try {
            actualIds = playlistDAO.getPlaylistIds(id);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        //checking orderedList content
        if(orderedList.size()!=actualIds.size()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        HashSet<Integer> map = new HashSet<>(actualIds);
        for(int songId : orderedList){
            if(!map.contains(songId)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        try {
            for(int i = 0; i < orderedList.size(); i++) {
                    playlistDAO.updatePosition(id,orderedList.get(i),i);
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
