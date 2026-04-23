package com.shashank.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.shashank.entity.UserAccount;
import com.shashank.repo.UserRepo;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService  {
	@Autowired
    private UserRepo userRepo;
	@Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // Get user details from Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String pictureUrl = oAuth2User.getAttribute("picture");
System.out.println(pictureUrl);
	     
        // Save user if not in DB
        userRepo.findById(email).orElseGet(() -> {
            UserAccount user = new UserAccount();
            user.setEmail(email);
            user.setName(name);
	         // Convert to bytes
	   	     byte[] photoBytes = null;
	   	     try (InputStream in = new URL(pictureUrl).openStream()) {
	   	         photoBytes = in.readAllBytes();
	   	     } catch (MalformedURLException e) {
	   			// TODO Auto-generated catch block
	   			e.printStackTrace();
	   		} catch (IOException e) {
	   			// TODO Auto-generated catch block
	   			e.printStackTrace();
	   		}
            user.setPhoto(photoBytes);
            user.setEnable(true);
            user.setRole("ROLE_USER");
            
            return userRepo.save(user);
        });
        
        // Always assign ROLE_USER
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "email"   // use "sub" if email not present
        );
        /*“I implemented a CustomOAuth2UserService to handle Google login.
           In loadUser(), I fetch the Google user details like email, name, and profile picture using DefaultOAuth2UserService.
           I check whether the user already exists in the database;
            if not, I download the Google profile picture using its URL, convert it into a byte array, and save it along with basic details like name, email, enable status, and role.
           Finally, I return a DefaultOAuth2User object with ROLE_USER authority so that Spring Security can treat this user as authenticated.”
         * 
         * 
         * 
         * 
         * 
         * 
         */
    }
}
