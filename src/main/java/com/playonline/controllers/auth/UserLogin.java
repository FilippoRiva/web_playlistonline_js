package com.playonline.controllers.auth;

import com.playonline.beans.User;
import com.playonline.dao.UserDAO;
import com.playonline.utils.Htmls;
import com.playonline.utils.MySQLConnections;

import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
@MultipartConfig
public class UserLogin extends HttpServlet{
    private Htmls htmls;
    private Connection connection;
    @Override
    public void init(){
        ServletContext context = getServletContext();
        this.htmls = Htmls.getInstance(context);
        this.connection = MySQLConnections.getConnection(context);
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.getWriter().write(htmls.loginPage);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //cookie check
        Cookie[] cookies = req.getCookies();
        if(cookies!=null) {
            for (Cookie c : cookies) {
                if (c.getName().equals("LOGIN_INFO")) {
                    User user;
                    try {
                        user = new UserDAO(connection).checkCookie(c.getValue());
                    } catch (SQLException e) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return;
                    }
                    if(user == null) break;
                    req.getSession(true).setAttribute("user", user);
                    resp.getWriter().write(user.getUsername());
                    return;
                }
            }
        }

        // input parsing
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if(username==null || password==null){
            resp.setStatus(401);
            resp.getWriter().write("please fill every field");
            return;
        }
        // validity check
        User user = null;
        try {
            user = new UserDAO(connection).checkCredentials(username,password);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if(user==null){
            resp.setStatus(401);
            resp.getWriter().write("wrong credentials");
            return;
        }
        // new auth cookie
        req.getSession(true).setAttribute("user",user);
        Cookie cookie = new Cookie("LOGIN_INFO",user.getCookie());
        cookie.setMaxAge(60*60*24*365);
        resp.addCookie(cookie);
        resp.getWriter().write(username);
    }
}
