package com.example.socialmediawebsemantique;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class UserModel {

    @Id
    private Integer id;
    private String nomUser;
    private String title;
    private String email;
    private Integer age;
    private String date;
}