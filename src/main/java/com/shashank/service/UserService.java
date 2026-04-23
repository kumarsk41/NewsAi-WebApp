package com.shashank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.shashank.entity.NewsCheck;
import com.shashank.entity.UserAccount;
import com.shashank.repo.UserRepo;

@Service
public class UserService {
	@Autowired
	UserRepo userRepo;
	
	@Autowired
	BCryptPasswordEncoder b;

	public boolean save(UserAccount userAccount) {
		if(userRepo.findById(userAccount.getEmail()).orElse(null)==null) {
			userAccount.setPassword(b.encode(userAccount.getPassword()));
			userRepo.save(userAccount);
			return true;
		}else{
			return false;
		}
	}

	public String getName(String email) {
		UserAccount u=userRepo.findById(email).orElse(null);
		if(u==null) {
			return null;
		}else{
			return u.getName();
		}
	}

	public UserAccount getUserByEmail(String email) {
		return userRepo.findById(email).orElse(null);
	}

	public void updatePhoto(String email, byte[] bytes) {
		UserAccount user=userRepo.findById(email).get();
		user.setPhoto(bytes);
		userRepo.save(user);
	}
	
	public void updateProfile(String email, String name,String phone) {
		UserAccount user=userRepo.findById(email).get();
		user.setName(name);
		user.setPhone(phone);
		userRepo.save(user);
	}
	public void updatePassword(String email,String password) {
		UserAccount user=userRepo.findById(email).get();
		user.setPassword(b.encode(password));
		userRepo.save(user);
	}
	public boolean checkOldPassword(String email,String password) {
		UserAccount user=userRepo.findById(email).get();
		if(b.matches(password, user.getPassword())) {
			return true;
		}else {
			return false;
		}
	}
	

	public byte[] getPhoto(String email) {
		UserAccount user=userRepo.findById(email).get();
		return user.getPhoto();
	}

	public String getPhone(String email) {
		UserAccount u=userRepo.findById(email).orElse(null);
		if(u==null) {
			return null;
		}else{
			return u.getPhone();
		}
	}
 
	public boolean checkPasswordExist(String email) {
		UserAccount u=userRepo.findById(email).orElse(null);
		if(u.getPassword()==null || u.getPassword().length()==0) {
			return false;
		}else{
			return true;
		}
	}
}
