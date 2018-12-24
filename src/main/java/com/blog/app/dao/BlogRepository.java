package com.blog.app.dao;

import com.blog.app.entity.BlogSchema;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends MongoRepository<BlogSchema, String> {
}
