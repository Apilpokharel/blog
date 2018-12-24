package com.blog.app.security;

import com.blog.app.dao.UserRepository;
import com.blog.app.entity.Tokens;
import com.blog.app.entity.UserSchema;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

@Service
public class JWTTokenGen {

    private final static String SECRET = "mysecrethere...";
    private UserRepository userRepository;

    public JWTTokenGen(UserRepository userRepository){
        this.userRepository = userRepository;

    }

    long milisec = System.currentTimeMillis();

    Date now = new Date(milisec);


    public Tokens generateAuthToken(String email) {

        Tokens tokens = new Tokens();

        UserSchema user = null;
        try {
            user = userRepository.findByEmail(email);
        }catch(Exception ex) {}
        if(user != null) {

            String access = "auth";
            String token = Jwts.builder()
                    .claim("_id", user.getId())
                    .claim("access", access)
                    .setIssuedAt(now)
                    .signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
                    .setHeaderParam("alg", "HS256")
                    .setHeaderParam("typ", "JWT")
                    .compact();

            tokens.setAccess(access);
            tokens.setToken(token);

            ArrayList<Tokens> arr = new  ArrayList<Tokens>();
            arr.add(tokens);
            user.setTokens(arr);

            userRepository.save(user);

            return tokens;
        }
        return null;
    }
}
