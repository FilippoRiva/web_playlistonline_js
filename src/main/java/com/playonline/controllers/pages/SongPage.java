package com.playonline.controllers.pages;

import com.playonline.beans.Song;
import com.playonline.beans.User;
import com.playonline.dao.SongDAO;
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

@WebServlet("/song")
public class SongPage extends HttpServlet {
    TemplateEngine templateEngine;
    JavaxServletWebApplication application;
    Connection connection;
    SongDAO songDAO;

    @Override
    public void init() {
        this.templateEngine = TemplateEngines.getTemplateEngine(getServletContext());
        this.application = JavaxServletWebApplication.buildApplication(getServletContext());
        try {
            this.connection = MySQLConnections.getConnection(getServletContext());
            this.songDAO = new SongDAO(connection,getInitParameter("filesDirectory"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if(user==null){
            resp.sendRedirect("login");
            return;
        }
        // input parsing
        String id_str = req.getParameter("id");
        int id;
        try{
            id = Integer.parseInt(id_str);
        } catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // page templating
        Song song;
        try {
            song = songDAO.getSongByID(id, user.getUsername());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if(song==null){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        IWebExchange webExchange = application.buildExchange(req,resp);
        WebContext context = new WebContext(webExchange);
        context.setVariable("song",song);
        templateEngine.process("song",context,resp.getWriter());
    }
}
