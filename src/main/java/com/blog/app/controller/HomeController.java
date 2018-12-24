package com.blog.app.controller;


import com.blog.app.dao.BlogRepository;
import com.blog.app.dao.CustomMongoRepo;
import com.blog.app.entity.BlogSchema;
import com.blog.app.entity.Mail;
import com.blog.app.entity.UserSchema;
import com.blog.app.filehandler.AWSUpload;
import com.blog.app.notification.HTMLEmailService;
import com.blog.app.security.CheckAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {



    private CheckAuth checkAuth;
    private BlogRepository blogRepository;
    private CustomMongoRepo cs;
    private AWSUpload aws;
    private HTMLEmailService emailService;



    @Autowired
    public HomeController(HTMLEmailService emailService, AWSUpload aws, CustomMongoRepo cs, CheckAuth checkAuth, BlogRepository blogRepository){
        this.checkAuth = checkAuth;
        this.blogRepository = blogRepository;
        this.cs = cs;
        this.aws = aws;
        this.emailService = emailService;
    }


    public UserSchema getUser(HttpServletRequest req){
        UserSchema user =null;
        try {
           user = (UserSchema) req.getAttribute("user");
        }catch (NullPointerException ex){}

        return user;
    }

    @GetMapping("/")
    public String homePage(Model model) throws ServletException, IOException {
     model.addAttribute("log","ake");
        return "Home";
    }


    @GetMapping("/addblog")
    public String addBlog(Model model) throws ServletException, IOException {
        checkAuth.Authentication();
        model.addAttribute("blog", new BlogSchema());
        return "addblog";
    }


    @PostMapping("/addblog")
    public String addblogPost(@Valid @ModelAttribute("blog")BlogSchema blogSchema, BindingResult bindingResult, HttpServletRequest req) throws ServletException, IOException {
        checkAuth.Authentication();

        if(bindingResult.hasErrors()){
            return "addblog";
        }
        blogSchema.setRefId(getUser(req).getEmail());
        blogSchema.setCreatedAt(new Date().toString());
        blogSchema.setVisited(0);
        blogSchema.setLikes(new ArrayList<String>());
        blogRepository.save(blogSchema);
        return "redirect:/";
    }



    @GetMapping("/feed")
    public String feed() throws ServletException, IOException {
        checkAuth.Authentication();
        return "feed";
    }
    
    
    @GetMapping("/blog/{haa}")
    public String blogInfo(@PathVariable("haa") String haa, Model model, HttpServletRequest req) throws IOException, MessagingException, ServletException {

        checkAuth.Authentication();

        cs.addVisited(haa);


    	model.addAttribute("bloginfo", cs.findBlog(haa));


    	BlogSchema blogSchema = cs.findBlog(haa);

if(getUser(req) != null) {
    Mail mail = new Mail();
    mail.setFrom("ApilBlog");
    mail.setTo(getUser(req).getEmail());
    mail.setSubject("blog Post Review");

    Map mod = new HashMap();
    mod.put("name", getUser(req).getUsername());
    mod.put("title", blogSchema.getTitle());
    mod.put("location", "nepal");
    mod.put("body", blogSchema.getBody());
    mod.put("likes", blogSchema.getLikes().size());
    mod.put("signature", "http://localhost:2000/blog/" + blogSchema.getId());
    mail.setModel(mod);

    emailService.sendSimpleMessage(mail);

}
		return "bloginfo";
    	
    }
    
    
    @GetMapping("/myblog")
    public String myBlog(Model model,HttpServletRequest req) throws ServletException, IOException {
    	checkAuth.Authentication();
    	model.addAttribute("user", getUser(req));
    	return "myblogs";
    }
    
    
    @PostMapping("/fileup")
    public String fileUp(@RequestParam("file") MultipartFile file,  RedirectAttributes redirectAttributes,  HttpServletRequest req) throws IOException, ServletException {
        checkAuth.Authentication();

    	String filename = null;
        if (!file.isEmpty()) {
             filename = this.aws.fileUpload(file);
             System.out.println("file name is "+filename);
             aws.deleteFileFromS3Bucket(getUser(req).getImage());
             cs.updateUserImg(filename, getUser(req).getId());
               
             return "redirect:/myblog";
        } else {
            redirectAttributes.addFlashAttribute("fileempty", "file is empty try adding another one");
            return "redirect:/myblog";
        }
    	
    }
}
