package com.example.socialmediawebsemantique;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class GroupModel {
    @Id
    private Integer id;
    private String name;
    private Integer capacity;
    private String date;
}