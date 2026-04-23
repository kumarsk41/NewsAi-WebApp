package com.shashank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserAccount {
	@Id
	private String email;
	private String password;
	private String name;
	private String phone;
    private String role="ROLE_USER";
    private boolean enable=true;
    @Column(nullable = true , columnDefinition = "longblob")
    private byte[] photo;
}
