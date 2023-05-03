package com.playonline.controllers.files;

import com.playonline.beans.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/cover")
public class CoverFile extends HttpServlet {
    String folder;
    @Override
    public void init() {
        this.folder = getServletContext().getInitParameter("filesDirectory");
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
        String ar_ID_str = req.getParameter("arID");
        String a_ID_str = req.getParameter("aID");
        int artistID;
        int albumID;
        if(a_ID_str==null || ar_ID_str==null){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try{
            artistID = Integer.parseInt(ar_ID_str);
            albumID = Integer.parseInt(a_ID_str);
        } catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // file check
        String path = folder+user.getUsername()+"/"+artistID+"/"+albumID+"/cover";
        File f = new File(path);
        if(!f.exists()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // response
        resp.setContentType(getServletContext().getMimeType(path));
        resp.setContentLength((int)f.length());
        try(FileInputStream input = new FileInputStream(f)){
            OutputStream output = resp.getOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while((count=input.read(buffer))>=0){
                output.write(buffer,0,count);
            }
        }
    }
}
