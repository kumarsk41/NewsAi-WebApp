package com.shashank.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;


@Component
public class MailSend {
	@Autowired
	private JavaMailSender mailSender;
	
	public String doMailSend(String remail,String sub,String body) {
		try {
			SimpleMailMessage mailMessage=new SimpleMailMessage();
			mailMessage.setTo(remail);
			mailMessage.setSubject(sub);
			mailMessage.setText(body);
			
			
			mailSender.send(mailMessage);
			return "success";
		}catch (Exception e) {
			e.printStackTrace();
			return "failed";
		}
	}
	public String doMailSendHTML(String remail,String sub,String body) {
		try {
			MimeMessage mailMessage=mailSender.createMimeMessage();
			boolean multiPart=true;
			MimeMessageHelper helper=new MimeMessageHelper(mailMessage,multiPart);
			helper.setTo(remail);
			helper.setSubject(sub);
			helper.setText("text/html",body);
			mailSender.send(mailMessage);
			return "success";
		}catch (Exception e) {
			e.printStackTrace();
			return "failed";
		}
	}
}
