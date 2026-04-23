package com.shashank.mail;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
//@ConfigurationProperties(prefix = "app")
@Data
public class MailServerConfig {
	private String adminGmailEmail;
    private String adminGmailPasscode;
	private String gEmail;
	
	//or
	
//	@Value("${app.admin-gmail-email}")
//	private String adminEmail;
	
	@Bean
	private JavaMailSender getMailConfig() {
		JavaMailSenderImpl mailSender=new JavaMailSenderImpl();//JavaMailSenderImpl object banaya
		mailSender.setHost("smtp.gmail.com");//SMTP Server details set kiye
		mailSender.setPort(587);
		
//		System.out.println(gEmail+"::"+adminGmailEmail+":"+adminGmailPasscode);
		mailSender.setUsername(adminGmailEmail);
		mailSender.setPassword(adminGmailPasscode);
		Properties prop=mailSender.getJavaMailProperties();//Ye ek Properties object deta hai
		                                                  // jisme hum SMTP settings (email sending ke rules) add karte hain.
		prop.put("mail.smtp.auth", "true");//SMTP authentication ON karega
		prop.put("mail.smtp.starttls.enable", "true");//Secure connection enable karta hai (TLS)
		//prop.put("mail.debug", "true");//optional
		return mailSender;
	}
}
