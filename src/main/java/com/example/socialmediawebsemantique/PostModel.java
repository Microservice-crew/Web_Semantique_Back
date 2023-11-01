package com.example.socialmediawebsemantique;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class PostModel {

    @Id
    private Integer id;
    private String nomUser;
    private String title;
    private String contenu;
    private String date;
}
