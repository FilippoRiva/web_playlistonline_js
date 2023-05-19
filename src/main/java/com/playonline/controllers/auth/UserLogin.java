package com.playonline.controllers.auth;

import com.playonline.beans.User;
import com.playonline.dao.UserDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public class UserLogin extends HttpServlet{

    private UserDAO userDAO;

    @Override
    public void init(){
        ServletContext context = getServletContext();

        Connection connection = MySQLConnections.getConnection(context);
        try {
            userDAO = new UserDAO(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth cookie check
        Cookie[] cookies = req.getCookies();
        if(cookies==null) {
            //todo redirect to homepage
            return;
        }
        for (Cookie c : cookies) {
            if (c.getName().equals("LOGIN_INFO")) {
                User user;
                try {
                    user = userDAO.checkCookie(c.getValue());
                } catch (SQLException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                if(user == null) break;
                req.getSession(true).setAttribute("user", user);
                resp.sendRedirect("/PlaylistOnlineJS/home");
                return;
            }
        }
        //todo redirect to homepage
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // input parsing
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if(username==null || password==null || password.length()<5){
            //todo failed auth
            return;
        }
        // validity check
        User user = null;
        try {
            user = userDAO.checkCredentials(username,password);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if(user==null){
            //todo failed auth
            return;
        }
        // new auth cookie
        req.getSession(true).setAttribute("user",user);
        Cookie cookie = new Cookie("LOGIN_INFO",user.getCookie());
        cookie.setMaxAge(60*60*24*365);
        resp.addCookie(cookie);
        resp.sendRedirect("/PlaylistOnlineJS/home");
    }
}
