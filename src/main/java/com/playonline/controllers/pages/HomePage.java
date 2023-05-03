package com.playonline.controllers.pages;

import com.playonline.beans.Playlist;
import com.playonline.beans.User;
import com.playonline.dao.AlbumDAO;
import com.playonline.dao.PlaylistDAO;
import com.playonline.dao.SongDAO;
import com.playonline.utils.GenreEnum;
import com.playonline.utils.MySQLConnections;
import com.playonline.utils.TemplateEngines;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;


@WebServlet("/home")
@MultipartConfig
public class HomePage extends HttpServlet {
    TemplateEngine templateEngine;
    JavaxServletWebApplication application;
    PlaylistDAO playlistDAO;
    AlbumDAO albumDAO;
    SongDAO songDAO;

    @Override
    public void init() {
        this.templateEngine = TemplateEngines.getTemplateEngine(getServletContext());
        this.application = JavaxServletWebApplication.buildApplication(getServletContext());
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

        // input parsing
        Object addingNewAlbum = req.getAttribute("addingNewAlbum");
        if(addingNewAlbum==null) {
            String att = req.getParameter("a");
            if(att!=null){
                addingNewAlbum =Boolean.parseBoolean(att);
                req.setAttribute("addingNewAlbum",addingNewAlbum);
            }
        }
        // keeping genre input
        String s_genre = req.getParameter("s_genre");
        int selectedId=0;
        if(s_genre!=null){
            for(GenreEnum g : GenreEnum.values()){
                if(Objects.equals(g.getName(), s_genre)){
                    selectedId=g.getId();
                }
            }
        }

        // getting user playlist
        List<Playlist> playlists;
        try {
            playlists = playlistDAO.getUserPlaylists(user.getUsername());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // page templating
        IWebExchange webExchange = application.buildExchange(req,resp);
        WebContext context = new WebContext(webExchange);
        context.setVariable("playlists",playlists);
        context.setVariable("adding", false);
        context.setVariable("genres",GenreEnum.values());
        if(addingNewAlbum!=null) {
            context.setVariable("adding", true);
            boolean b = (boolean) addingNewAlbum;
            if(b) {
                context.setVariable("newAlbum",true);
            }
            else {
                context.setVariable("newAlbum",false);
                try {
                    context.setVariable("albums",albumDAO.getUserAlbums(user.getUsername()));
                } catch (SQLException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            }
        }
        try {
            context.setVariable("artists",playlistDAO.getArtists(user.getUsername()));
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        context.setVariable("selectedId",selectedId);
        templateEngine.process("home",context,resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String att = req.getParameter("a");
        if(att!=null) {
            boolean addingNewAlbum;
            addingNewAlbum = Boolean.parseBoolean(att);
            req.setAttribute("addingNewAlbum", addingNewAlbum);
        }
        doGet(req,resp);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
