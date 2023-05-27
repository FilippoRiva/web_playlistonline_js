package com.playonline.utils;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Htmls {
    public final String loginPage;
    public final String homePage;
    private static Htmls instance;

    public Htmls(ServletContext context){
        try {
            Path file = Path.of(context.getResource("login.html").getPath());
            this.loginPage = Files.readString(file);
            file = Path.of(context.getResource("home.html").getPath());
            this.homePage = Files.readString(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Htmls getInstance(ServletContext context){
        if(instance==null){
            Htmls.instance = new Htmls(context);
        }
        return instance;
    }
}
