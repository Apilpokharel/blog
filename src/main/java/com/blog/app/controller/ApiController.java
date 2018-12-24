package com.blog.app.controller;


import com.blog.app.dao.CustomMongoRepo;
import com.blog.app.dao.UserRepository;
import com.blog.app.entity.BlogSchema;
import com.blog.app.entity.UserSchema;
import com.blog.app.security.CheckAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api")
public class ApiController {

    private CustomMongoRepo customMongoRepo;
   private CheckAuth checkAuth;
   private UserRepository userRepo;

    public ApiController(CheckAuth checkAuth, CustomMongoRepo customMongoRepo){
        this.customMongoRepo = customMongoRepo;
        this.checkAuth = checkAuth;
    }

    public UserSchema getUser(HttpServletRequest req) {
        UserSchema user = null;
        try {
            user = (UserSchema) req.getAttribute("user");
        } catch (Exception ex) {}

        return user;
    }


    @GetMapping("/recent_feed")
   public ResponseEntity<?> recent(HttpServletRequest req) throws ServletException, IOException {
        checkAuth.Authentication();
        
		ArrayList<ArrayList<Object>> arr = new ArrayList<ArrayList<Object>>();

        
       List <BlogSchema> bs = null;
        try {
        	bs =  customMongoRepo.findRecents();
        }catch(Exception ex) {}

        loppCreate(arr, bs);


        return ResponseEntity.ok().body(Arrays.asList(getUser(req), arr));
    }


    @GetMapping("/liked_feed")
    public ResponseEntity<?> liked(HttpServletRequest req) throws ServletException, IOException {
        checkAuth.Authentication();

        ArrayList<ArrayList<Object>> arr = new ArrayList<ArrayList<Object>>();

        
        List <BlogSchema> bs = null;
         try {
         	bs =  customMongoRepo.findLiked();
         }catch(Exception ex) {}

        loppCreate(arr, bs);


        return ResponseEntity.ok().body(Arrays.asList(getUser(req), arr));
    }

    @GetMapping("/visited_feed")
    public ResponseEntity<?> visited(HttpServletRequest req) throws ServletException, IOException {
        checkAuth.Authentication();
        ArrayList<ArrayList<Object>> arr = new ArrayList<ArrayList<Object>>();

        
        List <BlogSchema> bs = null;
         try {
         	bs =  customMongoRepo.findVisited();
         }catch(Exception ex) {}

        loppCreate(arr, bs);


        return ResponseEntity.ok().body(Arrays.asList(getUser(req), arr));
    }

    private void loppCreate(ArrayList<ArrayList<Object>> arr, List<BlogSchema> bs) {
        if(bs != null) {
            bs.forEach(new Consumer<BlogSchema>() {

                @Override
                public void accept(BlogSchema t) {
                    // TODO Auto-generated method stub
                    ArrayList<Object> obj = new ArrayList<Object>();
                    UserSchema user = customMongoRepo.findUserByBlog(t.getRefId());

                    obj.add(user);
                    obj.add(t);

                    arr.add(obj);
                }

            });
        }
    }


    @PostMapping("/addlike/{hee}")
    public String addlike(@PathVariable("hee") String id, HttpServletRequest req) throws ServletException, IOException {
    checkAuth.Authentication();
    customMongoRepo.addLike(id, getUser(req).getId());
    return "done succesfully";
    }

@GetMapping("/myblogs")
public ResponseEntity<?> myBlogs(HttpServletRequest req) throws ServletException, IOException{
	checkAuth.Authentication();
	List<BlogSchema> list = customMongoRepo.findMyAllBlogs(getUser(req).getEmail());
	return ResponseEntity.ok().body(Arrays.asList(getUser(req), list));
}
}
