package com.blog.app.dao;

import com.blog.app.entity.UserSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserSchema, String> {
    UserSchema findByEmail(String email);
}
