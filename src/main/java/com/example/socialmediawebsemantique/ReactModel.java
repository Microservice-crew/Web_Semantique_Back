package com.example.socialmediawebsemantique;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter

public class ReactModel {
    @Id
    private Integer id;
    private String title;
    private Integer nombrelike;
    private Integer nombredislike;
    private String date;
}
