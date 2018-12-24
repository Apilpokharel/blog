package com.blog.app.controller;


import com.blog.app.dao.CustomMongoRepo;
import com.blog.app.dao.UserRepository;
import com.blog.app.entity.Tokens;
import com.blog.app.entity.UserSchema;
import com.blog.app.notification.EmailService;
import com.blog.app.security.CheckAuth;
import com.blog.app.security.JWTTokenGen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

@Controller
public class AuthController {


    private UserRepository userRepository;
    private CheckAuth checkAuth;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private CustomMongoRepo customMongoRepo;
    private HttpSession httpSession;
    private JWTTokenGen jwtTokenGen;
    private EmailService emailService;
    @Autowired
    public AuthController(EmailService emailService, JWTTokenGen jwtTokenGen, CustomMongoRepo customMongoRepo, HttpSession httpSession, UserRepository userRepository, CheckAuth checkAuth, BCryptPasswordEncoder bCryptPasswordEncoder){
       this.userRepository = userRepository;
       this.checkAuth = checkAuth;
       this.bCryptPasswordEncoder = bCryptPasswordEncoder;
       this.customMongoRepo = customMongoRepo;
       this.httpSession = httpSession;
       this.emailService = emailService;
       this.jwtTokenGen =jwtTokenGen;
    }


    String code = null;







    //login

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(){
        String token = null;
        try {
            token = checkAuth.getToken();
        }catch(Exception ex){

        }

        System.out.println("token is "+token);
        if(token != null) {

            UserSchema sc = null;
            try {
                sc = customMongoRepo.findByToken(token);
            }catch(Exception ex) {}
            if(sc!=null) {
                if(sc.isChecked() == true){
                    return "redirect:/feed";
                }

                else{
                    return "redirect:/verification";
                }}

        }

        return "login";



    }





    //postlogin
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String postlogin(@RequestParam("email") String email, @RequestParam("password") String password, Model model, RedirectAttributes redirectAttributes, HttpServletResponse res) throws URISyntaxException, IOException {

        String token = null;
        try {
            token = checkAuth.getToken();
        }catch(Exception ex){

        }
        if(token != null) {
            UserSchema sc = null;
            try {
                sc = customMongoRepo.findByToken(token);
            }catch(Exception ex) {}
            if(sc!=null) {
                if(sc.isChecked() == true){
                    return "redirect:/feed";
                }
                else{
                    return "redirect:/verification";
                }
            }
        }


        UserSchema  sch =  userRepository.findByEmail(email);
        redirectAttributes.addFlashAttribute("pass", password);
        redirectAttributes.addFlashAttribute("email", email);

        if (sch != null) {
            if (bCryptPasswordEncoder.matches(password, sch.getPassword())) {
                try {
                    Tokens tokens = jwtTokenGen.generateAuthToken(email);
                    Cookie cookie = new Cookie("hijab", tokens.getToken());
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(60*60*60*3);
                    res.addCookie(cookie);
                }catch(Exception ex) {}
                if(sch.isChecked()==true) {
                    //gate ways for sending cookie to node serve

                    return "redirect:/feed";
                }
                else{
                    code =  new BigInteger(15, new SecureRandom()).toString(5);
                    try{
                        emailService.sendEmail(sch.getEmail(), code);
                    }catch(MailException em){

                        redirectAttributes.addFlashAttribute("gamil","your gmail is invalid enter a valid gmail");
                        return "redirect:/login";
                    }
                    httpSession.setAttribute("vcode", code);
                    return "redirect:/verification";
                }
            } else {

                redirectAttributes.addFlashAttribute("passfail", "wrong password");
                return "redirect:/login";
            }
        } else {

            redirectAttributes.addFlashAttribute("emailfail", "invalid email");
            return "redirect:/login";
        }






    }



    //register
    @RequestMapping(value = "/register", method= RequestMethod.GET)

    public String register(Model model, RedirectAttributes redirectAttributes){
        model.addAttribute("schema", new UserSchema());

        String token = null;
        try {
            token = checkAuth.getToken();
        }catch(Exception ex){

        }
        if(token != null) {
            UserSchema sc = null;
            try {
                sc = customMongoRepo.findByToken(token);
            }catch(Exception ex) {}
            if(sc!=null) {

                if(sc.isChecked() == true){
                    return "redirect:/feed";
                }
                else{
                    return "verify";
                }
            }
        }

        return "register";



    }






    //post register
    @RequestMapping(value = "/register", method= RequestMethod.POST)

    public String postregister(@Valid @ModelAttribute("schema") UserSchema sc, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, HttpServletRequest request, HttpServletResponse res){

        String token = null;
        try {
            token = checkAuth.getToken();
        }catch(Exception ex){

        }
        if(token != null) {
            UserSchema scc = null;
            try {
                scc = customMongoRepo.findByToken(token);
            }catch(Exception ex) {}
            if(scc!=null) {

                if(scc.isChecked() == true){
                    return "redirect:/feed";
                }
                else{
                    return "verify";
                }
            }
        }
        UserSchema schr = userRepository.findByEmail(sc.getEmail());

        redirectAttributes.addAttribute("sc", sc);
        if(bindingResult.hasErrors()) {

            return "register";
        }

        if(schr != null){
            redirectAttributes.addFlashAttribute("emailalready","email already blongs to other user try other one");

            return "redirect:/register";
        }
        else {
            sc.setPassword(bCryptPasswordEncoder.encode(sc.getPassword()));
            sc.setCreatedAt(new Date().toString());
            sc.setChecked(false);

            userRepository.save(sc);

            Tokens tokens = jwtTokenGen.generateAuthToken(sc.getEmail());
            Cookie cookie = new Cookie("hijab", tokens.getToken());
            cookie.setHttpOnly(true);
            cookie.setMaxAge(60*60*60*3);

            res.addCookie(cookie);

            code =  new BigInteger(15, new SecureRandom()).toString(5);
            try{
                emailService.sendEmail(sc.getEmail(), code);
            }catch(MailException em){
            }
            httpSession.setAttribute("vcode", code);

            return "redirect:/verification";

        }


    }


//verification

    @RequestMapping(value = "/verification", method = RequestMethod.GET)
    public String verification(){
        String token = null;
        try {
            token = checkAuth.getToken();
        }catch(Exception ex){

        }
        if(token != null) {
            UserSchema sc = null;
            try {
                sc = customMongoRepo.findByToken(token);
            }catch(Exception ex) {}
            if(sc!=null) {

                if(sc.isChecked() == true){
                    return "redirect:/feed";
                }
                else{
                    return "verify";
                }
            }
        }

        return "redirect:/login";



    }


    //verification Post
    @RequestMapping(value = "/verification", method = RequestMethod.POST)
    public String postverification(@RequestParam("vcode") String vcode, RedirectAttributes redirectAttributes,HttpServletResponse res) throws URISyntaxException, IOException {

        String token = null;
        try {
            token = checkAuth.getToken();
        }catch(Exception ex){

        }
        if(token != null) {
            UserSchema sc = null;
            try {
                sc = customMongoRepo.findByToken(token);
            }catch(Exception ex) {}
            if(sc!=null) {
                if(sc.isChecked() == true){
                    return "redirect:/feed";
                }
                else{


                    String code =null;
                    try {
                        code = httpSession.getAttribute("vcode").toString();
                    }catch(Exception ex) {}

                    if(code == null) {
                        return "redirect:/login";
                    }
                    else {
                        if(code.equals(vcode)){


                            sc.setChecked(true);
                            sc.setImage("images.png");
                            userRepository.save(sc);




                            return "redirect:/feed";

                        }
                        else{
                            redirectAttributes.addFlashAttribute("msg", "The verification code you entered didn't matched ! Try Again");
                            return "redirect:/verification";
                        }
                    }
                }
            }
        }

        return "redirect:/login";



    }






    //verification sendcode again
    @RequestMapping(value = "/verification/{text}", method = RequestMethod.GET)
    public String sendCode(@PathVariable("text") String text, RedirectAttributes redirectAttributes){

        String token = null;
        try {
            token = checkAuth.getToken();
        }catch(Exception ex){

        }
        if(token != null) {
            UserSchema sc = null;
            try {
                sc = customMongoRepo.findByToken(token);
            }catch(Exception ex) {}
            if(sc!=null) {
                if(sc.isChecked() == true){
                    return "redirect:/feed";
                }
                else{
                    if (text.equals("send")) {
                        code =  new BigInteger(15, new SecureRandom()).toString(5);
                        try{
                            emailService.sendEmail(sc.getEmail(), code);
                        }catch(MailException em){


                            redirectAttributes.addFlashAttribute("gmail","your gmail is invalid enter a valid gmail");

                        }
                        httpSession.setAttribute("vcode", code);
                        return "redirect:/verification";
                    }else {
                        return "redirect:/verification";
                    }
                }

            }
        }

        return "redirect:/login";





    }






    //logout

    @RequestMapping(value = "/logoutt", method = RequestMethod.GET)
    public String logout(HttpServletRequest req, HttpServletResponse res){
        String token = null;
        try {
            token = checkAuth.getToken();
        }catch(Exception ex){

        }
        if(token != null) {
            UserSchema sc = null;
            try {
                sc = customMongoRepo.findByToken(token);
            }catch(Exception ex) {}
            if(sc!=null) {
                if(sc.isChecked() == true){
                    customMongoRepo.removeToken(sc.getEmail(), token);
                    try {
                        Cookie[] cookies = req.getCookies();
                        for(Cookie cookie: cookies) {
                            if(cookie.getName().equals("hijab")) {
                                cookie.setMaxAge(0);
                                res.addCookie(cookie);
                            }
                        }
                    }catch(Exception ex) {}
                    return "redirect:/login";
                }
                else{
                    return "verification";
                }
            }
        }

        return "redirect:/login";



    }



}
