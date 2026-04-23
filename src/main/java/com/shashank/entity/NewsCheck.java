package com.shashank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NewsCheck {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, length = 20000)
    private String articleText;

    @Lob
    @Column(length = 5000)
    private String summary;

    @Column(length = 32)
    private String credibility; // Credible / Suspicious / Fake

    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserAccount user;
}