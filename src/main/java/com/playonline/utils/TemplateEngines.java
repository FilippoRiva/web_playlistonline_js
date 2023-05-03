package com.playonline.utils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletContext;

public class TemplateEngines {
    public static TemplateEngine getTemplateEngine(ServletContext context){
        TemplateEngine templateEngine = new TemplateEngine();
        JavaxServletWebApplication application = JavaxServletWebApplication.buildApplication(context);
        WebApplicationTemplateResolver templateResolver  = new WebApplicationTemplateResolver(application);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
