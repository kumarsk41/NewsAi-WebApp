package com.shashank.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shashank.entity.NewsCheck;
import com.shashank.entity.UserAccount;

@Repository
public interface NewsRepo extends JpaRepository<NewsCheck, Long> {

	List<NewsCheck> findByUserOrderByCreatedAtDesc(UserAccount user);
}
