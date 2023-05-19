package com.playonline.controllers.auth;

import com.playonline.dao.UserDAO;
import com.playonline.utils.MySQLConnections;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet("/registration")
public class UserRegistration extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() {
        Connection connection = MySQLConnections.getConnection(getServletContext());
        try {
            userDAO = new UserDAO(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String passwordCopy = req.getParameter("passwordCopy");
        // input parsing
        if(username==null || username.length()<1 || username.length()>30) {
            //todo invalid username
            return;
        }
        if(password==null || password.length()<5 || password.length()>100){
            //todo invalid password
            return;
        }
        if(passwordCopy==null || password.compareTo(passwordCopy)!=0){
            //todo password not match
            return;
        }
        // registration attempt
        String cookieVal;
        try {
            if(!userDAO.isUsernameAvailable(username)){
                //todo username not available
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
        resp.sendRedirect("/PlaylistOnlineJS/login");
    }

}