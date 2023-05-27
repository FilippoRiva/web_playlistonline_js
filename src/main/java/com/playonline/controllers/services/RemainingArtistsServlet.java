package com.playonline.controllers.services;

import com.google.gson.Gson;
import com.playonline.beans.Artist;
import com.playonline.beans.User;
import com.playonline.dao.PlaylistDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/remaining-artists")
public class RemainingArtistsServlet extends HttpServlet {
    PlaylistDAO playlistDAO;
    @Override
    public void init() throws ServletException {
        try {
            this.playlistDAO = new PlaylistDAO(MySQLConnections.getConnection(getServletContext()));
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
        String p_ID_str = req.getParameter("p_ID");
        int p_ID;
        try{
            p_ID = Integer.parseInt(p_ID_str);
        } catch (NumberFormatException e){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("playlist ID is not a valid");
            return;
        }
        List<Artist> remainingArtists;
        try {
            remainingArtists = playlistDAO.getRemainingArtists(p_ID,user.getUsername());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(remainingArtists));
    }
}
