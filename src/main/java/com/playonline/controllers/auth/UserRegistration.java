package com.playonline.controllers.auth;

import com.playonline.dao.UserDAO;
import com.playonline.utils.MySQLConnections;
import com.playonline.utils.TemplateEngines;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet("/registration")
public class UserRegistration extends HttpServlet {

    TemplateEngine templateEngine;
    JavaxServletWebApplication application;
    private UserDAO userDAO;

    @Override
    public void init() {
        templateEngine = TemplateEngines.getTemplateEngine(getServletContext());
        application = JavaxServletWebApplication.buildApplication(getServletContext());
        Connection connection = MySQLConnections.getConnection(getServletContext());
        try {
            userDAO = new UserDAO(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        IWebContext context = new WebContext(application.buildExchange(req,resp));
        templateEngine.process("registration",context,resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String passwordCopy = req.getParameter("passwordCopy");
        WebContext context = new WebContext(application.buildExchange(req,resp));
        // input parsing
        if(username==null || username.length()<1 || username.length()>30) {
            context.setVariable("usernameInvalid",true);
            req.setAttribute("password",password);
            req.setAttribute("passwordCopy",passwordCopy);
            templateEngine.process("registration",context,resp.getWriter());
            return;
        }
        if(password==null || password.length()<5 || password.length()>100){
            req.setAttribute("username",username);
            context.setVariable("passwordInvalid",true);
            templateEngine.process("registration",context,resp.getWriter());
            return;
        }
        if(passwordCopy==null || password.compareTo(passwordCopy)!=0){
            req.setAttribute("username",username);
            req.setAttribute("password",password);
            context.setVariable("passwordCopyInvalid",true);
            templateEngine.process("registration",context,resp.getWriter());
            return;
        }
        // registration attempt
        String cookieVal;
        try {
            if(!userDAO.isUsernameAvailable(username)){
                req.setAttribute("username",username);
                req.setAttribute("password",password);
                req.setAttribute("passwordCopy",passwordCopy);
                context.setVariable("usernameUnavailable",true);
                templateEngine.process("registration",context,resp.getWriter());
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