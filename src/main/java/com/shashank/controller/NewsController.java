package com.shashank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shashank.entity.NewsCheck;
import com.shashank.service.NewsService;

import jakarta.servlet.http.HttpSession;


@Controller
public class NewsController {
	private final NewsService newsService;
	public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }
	
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
    	String email=(String)session.getAttribute("email");
       model.addAttribute("history", newsService.findByUserOrderByCreatedAtDesc(email));
        return "dashboard";
    }
    @GetMapping("/allnews")
    public String allnews( Model model) {
        model.addAttribute("allnews", newsService.findAll());
        return "allnews";
    }
    @PostMapping("/analyze")
    public String analyze(@RequestParam String article,HttpSession session, Model model) {
    	String email=(String)session.getAttribute("email");
    	
    	NewsCheck item = newsService.analyze(article, email);

        model.addAttribute("summary", item.getSummary());
        model.addAttribute("credibility", item.getCredibility());       

        return "news";
    }
    
    @GetMapping("/getDetails")
	public String getMethodName(@RequestParam long id,Model m) {
		NewsCheck newsCheck=newsService.getNewsById(id);
		m.addAttribute("newsDetails",newsCheck);
		return "news-details";
	}
    
    
    @GetMapping("/DeleteNews")
	public String deleteNews(@RequestParam long id,HttpSession session,Model m) {
    	
    	newsService.deleteNews(id);
    	String email=(String)session.getAttribute("email");
        m.addAttribute("history", newsService.findByUserOrderByCreatedAtDesc(email));
		return "dashboard";
	}
}
