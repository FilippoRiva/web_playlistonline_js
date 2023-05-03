package com.playonline.controllers.pages;

import com.playonline.beans.Artist;
import com.playonline.beans.Playlist;
import com.playonline.beans.User;
import com.playonline.dao.PlaylistDAO;
import com.playonline.utils.MySQLConnections;
import com.playonline.utils.TemplateEngines;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/playlist")
public class PlaylistPage extends HttpServlet {
    TemplateEngine templateEngine;
    JavaxServletWebApplication application;
    Connection connection;
    PlaylistDAO playlistDAO;
    @Override
    public void init(){
        this.templateEngine = TemplateEngines.getTemplateEngine(getServletContext());
        this.application = JavaxServletWebApplication.buildApplication(getServletContext());
        try {
            this.connection = MySQLConnections.getConnection(getServletContext());
            this.playlistDAO = new PlaylistDAO(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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
        String page_str = req.getParameter("page");
        int p_ID;
        int page;
        int prevPage;
        int nextPage;
        try {
            p_ID = Integer.parseInt(p_ID_str);
            page = Integer.parseInt(page_str);
            prevPage = page-1;
            nextPage = page+1;
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // page templating
        Playlist playlist;
        List<Artist> remainingArtists;
        boolean hasNext = false;
        boolean empty = false;
        try {
            playlist = playlistDAO.getPlaylistPage(p_ID, user.getUsername(), page);
            if(playlist==null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            remainingArtists = playlistDAO.getRemainingArtists(p_ID,user.getUsername());
            if(playlist.getSongs()==null){
                empty = true;
            }
            else if(playlist.getSongs().size()==6){
                playlist.setSongs(playlist.getSongs().subList(0,5));
                hasNext = true;
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        IWebExchange webExchange = application.buildExchange(req,resp);
        WebContext context = new WebContext(webExchange);
        context.setVariable("playlist", playlist);
        context.setVariable("artists", remainingArtists);
        context.setVariable("hasNext", hasNext);
        context.setVariable("prevPage", prevPage);
        context.setVariable("nextPage", nextPage);
        context.setVariable("emptyPlaylist", empty);
        templateEngine.process("playlist", context, resp.getWriter());
    }
}
