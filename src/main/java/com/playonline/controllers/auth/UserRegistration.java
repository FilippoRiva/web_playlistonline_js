package com.playonline.controllers.auth;

import com.playonline.dao.UserDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/registration")
@MultipartConfig
public class UserRegistration extends HttpServlet {
    private Connection connection;

    @Override
    public void init() {
        this.connection = MySQLConnections.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String passwordCopy = req.getParameter("passwordCopy");
        PrintWriter out = resp.getWriter();
        // input parsing
        if(username==null || username.length()<1 || username.length()>30) {
            resp.setStatus(401);
            out.print("invalid username");
            return;
        }
        if(password==null || password.length()<5 || password.length()>100){
            resp.setStatus(401);
            out.print("invalid password");
            return;
        }
        if(passwordCopy==null || password.compareTo(passwordCopy)!=0){
            resp.setStatus(401);
            out.print("passwords do not match");
            return;
        }
        // registration attempt
        String cookieVal;
        try {
            UserDAO userDAO = new UserDAO(connection);
            if(!userDAO.isUsernameAvailable(username)){
                resp.setStatus(401);
                out.print("username not available");
                return;
            } else {
                cookieVal = userDAO.getNewCookie();
                userDAO.addUser(username,password,cookieVal);
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        // auth cookie
        Cookie newCookie = new Cookie("LOGIN_INFO",cookieVal);
        newCookie.setMaxAge(60*60*24*365);
        resp.addCookie(newCookie);
        resp.getWriter().write(username);
        resp.sendRedirect("/PlaylistOnlineJS/login");
    }

}