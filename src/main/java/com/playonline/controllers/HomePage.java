package com.playonline.controllers;

import com.playonline.beans.User;
import com.playonline.utils.Htmls;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/home")
@MultipartConfig
public class HomePage extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // auth check
        User user = (User) req.getSession().getAttribute("user");
        if(user == null) {
            resp.sendRedirect("/PlaylistOnlineJS/login");
            return;
        }

        resp.getWriter().print(Htmls.getInstance(getServletContext()).homePage);
    }
}
