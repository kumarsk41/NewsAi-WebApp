package com.shashank.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.shashank.entity.NewsCheck;
import com.shashank.entity.UserAccount;
import com.shashank.mail.MailSend;
import com.shashank.service.UserService;
import com.shashank.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	UserService userService;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	MailSend mailSend;
	/*
	AuthenticationManager → login verify karne ke liye

	UserService → database me user related operations

	JwtUtil → forgot password ke token banane aur verify karne ke liye

	MailSend → email bhejne ke liye
	*/
	@PostMapping("/forgotPassword")
	public String forgotPassword(@RequestParam String email, ModelMap model) {
		/*Flow:

            1.  User email deta hai

            2.  DB me user search hota hai

            3.  Agar user nahi mila → error

            4.  Agar mila → JWT token generate

            5.  Reset link email me send
		 * 
		 */
		UserAccount userAccount= userService.getUserByEmail(email);
		if(userAccount==null) {
			model.addAttribute("showForgotModal",true);
			model.addAttribute("error2",true);
			return "index";
		}else {
			String token = jwtUtil.generateToken(email);
	        String resetLink = "http://localhost:2222/reset-password?token=" + token;
			
	        // Send email
	        String sub="Password Reset Request";
	        String body="Click the link to reset your password: " + resetLink;
	        mailSend.doMailSend(email, sub, body);
			model.addAttribute("msg","Reset link sent. Check your mail box!");
			return "index";
		}
	}
	
	
	@GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(defaultValue = "") String token, Model model) {
        String email = jwtUtil.validateToken(token);
        if (email == null) {
            model.addAttribute("msg", "Invalid or expired token.");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }
	
	
	@PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,@RequestParam String password,Model model) {
        String email = jwtUtil.validateToken(token);
        if (email == null) {
            model.addAttribute("msg", "Invalid or expired token.");
            return "reset-password";
        }
        userService.updatePassword(email, password);
        model.addAttribute("msg", "Password updated successfully. Please login.");
        model.addAttribute("showLoginModal",true);
        return "index";
    }
	
	@GetMapping(value = {"/","index"})
	public String home() {
		return "index";
	}
	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("showLoginModal",true);
		return "index";
	}
	@GetMapping("/user-home")
	public String userHome(HttpSession session) {
		return "user-home";
	}
	@GetMapping("/loginsucess")
	public String userHome(Authentication authentication,HttpSession session) {
		String email=authentication.getName();
		String name= userService.getName(email);
		String phone= userService.getPhone(email);
		session.setAttribute("name", name);
		session.setAttribute("email", email);
		session.setAttribute("phone", phone);
		session.setAttribute("passwordFlag", true);
		return "redirect:/user-home";
	}
	@GetMapping("/oauth2success")
	public String oauth2(@AuthenticationPrincipal OAuth2User principal,HttpSession session) {
		String email = principal.getAttribute("email");
		UserAccount user=userService.getUserByEmail(email);
		session.setAttribute("name", user.getName());
		session.setAttribute("email", email);
		session.setAttribute("phone", user.getPhone());
		session.setAttribute("passwordFlag", userService.checkPasswordExist(email));
		
		return "redirect:/user-home";
	}
	
	
	@PostMapping("/Register")
	public String register(@ModelAttribute UserAccount userAccount,HttpSession session, Model model) {
		String rawPassword=userAccount.getPassword();
		if(userService.save(userAccount)) {
			//mail send code
			mailSend(userAccount);
			
			//add code for authorization and role
			// Step 1: Create Authentication object
	        UsernamePasswordAuthenticationToken authToken =
	                new UsernamePasswordAuthenticationToken(
	                        userAccount.getEmail(),rawPassword
	                );
	        // Step 2: Authenticate using AuthenticationManager
	        Authentication authentication = authenticationManager.authenticate(authToken);
	        
	        // Step 3: Put authentication into SecurityContext
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        
	        // Step 4: Store in session (so Spring Security knows user is logged in)
	        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
	                             SecurityContextHolder.getContext());
			
			session.setAttribute("user", userAccount.getName());
			session.setAttribute("email", userAccount.getEmail());
			session.setAttribute("phone", userAccount.getPhone());
			session.setAttribute("passwordFlag", true);
//			model.addAttribute("msg","Registered Successfully!"); //does not work for redirect url
			return "redirect:/user-home";
		}else {
			model.addAttribute("msg","Email ID Already Registered!");
			return "index";
		}
		
	}
	@GetMapping("/403")
	public String accessDenied() {
		return "403";
	}
	
//	@PostMapping("/forgotPassword")
//	public String forgotPassword(@RequestParam String email, ModelMap model) {
//		UserAccount userAccount= userService.getUserByEmail(email);
//		if(userAccount==null) {
//			model.addAttribute("showForgotModal",true);
//			model.addAttribute("error2",true);
//			return "index";
//			
//			//or
//			
////			return "redirect:/index?error2=true&showForgotModal=true";
//		}else {
//			// Generate random password
//			String randomPass = UUID.randomUUID().toString().substring(0, 6);
//			
//			String sub="Password Generated!";
//			String body="Congrats! "+userAccount.getName()+", This is your new Password: "+randomPass;
//			mailSend.doMailSend(userAccount.getEmail(), sub, body);
//			model.addAttribute("msg","Password Sent! Check your mail box!");
//			return "index";
//		}
//	}
	
	@PostMapping("/UpdatePhoto")
	public String UpdatePhoto(@RequestPart MultipartFile photo,HttpSession session, Model model) throws IOException {
		String email=(String)session.getAttribute("email");
		userService.updatePhoto(email,photo.getBytes());
		model.addAttribute("msg","Success!");
		return "user-home";
	}
	@PostMapping("/UpdateProfile")
	public String updateProfile(@RequestParam String name,@RequestParam String phone,HttpSession session, Model model) {
		String email=(String)session.getAttribute("email");
		userService.updateProfile(email, name, phone);
		session.setAttribute("name", name);
		session.setAttribute("phone", phone);
		model.addAttribute("msg","Success!");
		return "user-home";
	}
	@PostMapping("/UpdatePassword")
	public String updatePassword(@RequestParam(defaultValue = "") String oldpassword,@RequestParam String newpassword,HttpSession session, Model model) {
		String email=(String)session.getAttribute("email");
		if(oldpassword.equals("") || userService.checkOldPassword(email, oldpassword)) {
			userService.updatePassword(email, newpassword);
			session.setAttribute("passwordFlag", true);
			model.addAttribute("msg","Success!");
		}else {
			model.addAttribute("msg","Old Password is Wrong!");
		}
		return "user-home";
		
	}
	@GetMapping("/getPhoto")
	public void getPhoto(@RequestParam String email,HttpServletResponse response) throws IOException {
		byte[] photo=userService.getPhoto(email);
		if(photo==null || photo.length==0) {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("static/person.jpg");
			photo=is.readAllBytes();/*InputStream is se jitni bhi image ki bytes aati hain,
			un sabko read karke ek byte[] array me convert kar deta hai
			aur usko variable photo me store kar deta hai.
			*/
		}
		response.getOutputStream().write(photo);
		/*response.getOutputStream()

          Ye method tumhe output stream deta hai

          Jisme tum data likh sakte ho jo browser ko jayega

         Think of it as:

        “Pipe jisse server data client ko bhejta hai”
		 */
		/*.write(photo)

           photo ek byte array hai

           Usme poori image stored hai

           .write() us byte array ko stream me daal deta hai
		 */ 
		 
	}
	
	
	
	private void mailSend(UserAccount userAccount) {
	//1.	Ye ek private helper method hai jo registration ke baad user ko email bhejne ka kaam karta hai.
		String sub="Registered Successfully!";//Step 1 — Email ka Subject set karna
		
//		String body="Congrats! "+userAccount.getName()+" have Registered Successfully!";
//		mailSend.doMailSend(userAccount.getEmail(), sub, body);
		
//		String body="<h1 style='background-color:blue;color:white;padding:20px;'>Congrats!</h1> "
//				+ "<p style='background-color:yellow;padding:20px;'>"+userAccount.getName()+" have Registered Successfully!</p>";
		
		
		//Step 2 — HTML Body banana
		String body="""
				<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Welcome to newsAi</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    body {
      margin: 0;
      padding: 0;
      background-color: #f4f6f8;
      font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
      color: #1a1a1a;
    }

    .container {
      max-width: 640px;
      margin: 0 auto;
      background-color: #ffffff;
    }

    .hero {
      background: url('https://images.unsplash.com/photo-1557683316-973673baf926') center center/cover no-repeat;
      height: 240px;
      display: flex;
      align-items: center;
      justify-content: center;
      text-align: center;
      color: white;
      padding: 20px;
    }

    .hero h1 {
      font-size: 28px;
      font-weight: bold;
      background: rgba(15, 23, 36, 0.7);
      padding: 12px 20px;
      border-radius: 8px;
    }

    .section {
      padding: 40px 30px;
    }

    .section h2 {
      font-size: 22px;
      color: #0f1724;
      margin-bottom: 10px;
    }

    .section p {
      font-size: 16px;
      line-height: 1.6;
      color: #333;
    }

    .link-block {
      background-color: #f0f4ff;
      padding: 18px;
      margin: 24px 0;
      border-left: 4px solid #0066ff;
    }

    .link-block a {
      font-weight: bold;
      color: #0066ff;
      word-break: break-word;
      text-decoration: underline;
    }

    .features {
      background-color: #0f1724;
      color: #ffffff;
      padding: 40px 30px;
    }

    .features h3 {
      font-size: 20px;
      margin-bottom: 24px;
      text-align: center;
    }

    .feature-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      justify-content: center;
    }

    .feature {
      flex: 0 0 250px;
      background-color: #1e293b;
      padding: 20px;
      border-radius: 8px;
    }

    .feature h4 {
      margin-top: 0;
      font-size: 16px;
      color: #ffffff;
    }

    .feature p {
      font-size: 14px;
      color: #d1d5db;
    }

    .footer {
      text-align: center;
      padding: 20px;
      background-color: #f9fafb;
      font-size: 13px;
      color: #7a8594;
    }

    .footer a {
      color: #0066ff;
      text-decoration: none;
    }

    .credit {
      font-size: 12px;
      margin-top: 10px;
      color: #a1a1aa;
    }

    @media (max-width: 600px) {
      .section, .features {
        padding: 24px 20px;
      }

      .feature-grid {
        flex-direction: column;
        align-items: stretch;
      }

      .feature {
        flex: 1 1 100%;
      }
    }
  </style>
</head>
<body>

  <div class="container">

    <!-- Hero -->
    <div class="hero">
      <h1>Welcome to newsAi</h1>
    </div>

    <!-- Intro Section -->
    <div class="section">
      <h2>Hello</h2>
      
      """+
      userAccount.getName()//Step 3 — User ka naam inject karna
        +
      """
      <p>Thanks for signing up with <strong>newsAi</strong> — your personalized AI-powered news companion. We're thrilled to have you on board.</p>


      <p>If you didn’t sign up for this account, feel free to ignore this message.</p>
    </div>

    <!-- Feature Section -->
    <div class="features">
      <h3>What You Can Do Next</h3>
      <div class="feature-grid">
        <div class="feature">
          <h4>🎯 Set Your Interests</h4>
          <p>Personalize your feed by selecting categories you care about most.</p>
        </div>
        <div class="feature">
          <h4>🔔 Enable Notifications</h4>
          <p>Stay on top of breaking news as it happens, right from your device.</p>
        </div>
        <div class="feature">
          <h4>🧠 Explore Topics</h4>
          <p>Get curated summaries and deep dives into trending global issues.</p>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="footer">
      Need help? Email us at <a href="mailto:aryansharma9075@gmail.com">support@newsai.example</a><br><br>
      &copy; {{current_year}} newsAi. All rights reserved.

      <div class="credit">
        Designed by Aryan Sharma.
      </div>
    </div>

  </div>

</body>
</html>

				""";
		//Step 4 — HTML mail send karna
	//Ye MailSend class ka method call karta hai jo HTML email SMTP server ke through send karta hai.
		mailSend.doMailSendHTML(userAccount.getEmail(), sub, body);
		
	}
}
