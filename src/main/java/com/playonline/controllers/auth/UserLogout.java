package com.playonline.controllers.auth;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/logout")
public class UserLogout extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // removes auth cookie and user session attribute
        Cookie cookie = new Cookie("LOGIN_INFO","0");
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
        req.getSession(true).setAttribute("user",null);
        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.sendRedirect("/PlaylistOnlineJS/login");
    }
}
