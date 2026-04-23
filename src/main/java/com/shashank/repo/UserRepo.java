package com.shashank.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shashank.entity.UserAccount;

@Repository
public interface UserRepo extends JpaRepository<UserAccount, String> {

	
}
