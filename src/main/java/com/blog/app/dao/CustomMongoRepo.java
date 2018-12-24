package com.blog.app.dao;


import com.blog.app.entity.BlogSchema;
import com.blog.app.entity.Tokens;
import com.blog.app.entity.UserSchema;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomMongoRepo {




    @Autowired
    MongoTemplate mongoTemplate;

    public UserSchema findByToken(String token) {
        Object id = null;
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey("mysecrethere...".getBytes())
                    .parseClaimsJws(token);

            id = claims.getBody().get("_id");
        } catch(Exception ex) {}

        UserSchema user = null;
        try {
            new Criteria();
            user = mongoTemplate.findOne(new Query( Criteria.where("_id").is(id)).addCriteria( Criteria.where("tokens.token").is(token)), UserSchema.class);
        }catch(Exception ex) {}

        return user;
    }

    public void removeToken(String email, String token) {
        Tokens tokens = new Tokens();
        tokens.setAccess("auth");
        tokens.setToken(token);
        Query query = new Query(Criteria.where("email").is(email));
        Update update = new Update().pull("tokens", tokens);
        mongoTemplate.updateFirst(query, update, UserSchema.class);
    }



    public void updateUserImg(String filename, String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        new Update();
        Update update = Update.update("image", filename);
        mongoTemplate.updateFirst(query, update, UserSchema.class);
    }


    public List<BlogSchema> findRecents(){
        Query query = new Query().with(new Sort(Sort.Direction.DESC, "_id")).limit(20);

        return mongoTemplate.find(query, BlogSchema.class);
    }


    public List<BlogSchema> findLiked(){
        Query query = new Query().with(new Sort(Sort.Direction.DESC, "likes")).limit(20);

        return mongoTemplate.find(query, BlogSchema.class);
    }


    public List<BlogSchema> findVisited(){
        Query query = new Query().with( new Sort(Sort.Direction.DESC, "visited")).limit(20);

        return mongoTemplate.find(query, BlogSchema.class);
    }


    public void addLike(String id, String userid){
        Query query = new Query(Criteria.where("_id").is(id));
        BlogSchema blogSchema = mongoTemplate.findOne(query, BlogSchema.class);
        if(blogSchema.getLikes().contains(userid)){
            mongoTemplate.updateFirst(query, new Update().pull("likes",userid), BlogSchema.class);
        }else{
            mongoTemplate.updateFirst(query, new Update().addToSet("likes",userid), BlogSchema.class);

        }
    }
    
    
    public BlogSchema findBlog(String id) {
    	return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), BlogSchema.class);
    }
    
    
    public void addVisited(String id) {
    	 Query query = new Query(Criteria.where("_id").is(id));
    	 BlogSchema bs = mongoTemplate.findOne(query, BlogSchema.class);
    	
    	 int newVisited = bs.getVisited() + 1;
    	 mongoTemplate.updateFirst(query, new Update().set("visited", newVisited), BlogSchema.class);
    }
    
    
    
    public List<BlogSchema> findMyAllBlogs(String email) {
    	return mongoTemplate.find(new Query(Criteria.where("refId").is(email)), BlogSchema.class);
    }
    
    public UserSchema findUserByBlog(String email) {
    	
    	return mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), UserSchema.class);
    }
    
    public void saveImage(String name, String id) {
    	Query query = new Query(Criteria.where("_id").is(id));
    	mongoTemplate.updateFirst(query, new Update().set("image", name), BlogSchema.class);
    }
}
