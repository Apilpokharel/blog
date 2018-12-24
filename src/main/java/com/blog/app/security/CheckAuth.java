package com.blog.app.security;


import com.blog.app.dao.CustomMongoRepo;
import com.blog.app.entity.UserSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@Service
public class CheckAuth {


    private HttpServletRequest req;
    private HttpServletResponse res;
    private CustomMongoRepo customMongoRepo;

    @Autowired
    public CheckAuth(HttpServletRequest req, HttpServletResponse res, CustomMongoRepo customMongoRepo) {
        this.req = req;
        this.customMongoRepo = customMongoRepo;
        this.res = res;
    }


    public String getToken() {
        String token = null;

        token = getFromCookie();

        return token;
    }

    private String getFromCookie() {

        String token = null;
        try {
            Cookie[] cookies = req.getCookies();
            for(Cookie cookie: cookies) {
                if(cookie.getName().equals("hijab")) {
                    token = cookie.getValue().toString();
                }
            }
        }catch(Exception ex) {}
        return token;
    }

    ;





    public void Authentication() throws ServletException, IOException {

        System.out.println("im in Authentication route");
        String token = null;

        token = getFromCookie();

        System.out.println("token is "+token);

        if(token != null) {
            UserSchema user = null;
            try {

                user = customMongoRepo.findByToken(token);

            } catch(Exception ex) {}

            if(user != null) {


                boolean checked = user.isChecked();

                if(checked == true) {
                    req.setAttribute("email", user.getEmail());
                    req.setAttribute("user", user);
                }
                else {
                    req.getRequestDispatcher("/verification").forward(req, res);
                }
            }
            else {
                res.sendRedirect("/login");
            }
        }
        else {
            System.out.println("sending redirect");
            res.sendRedirect("/login");

        }


    }
}
