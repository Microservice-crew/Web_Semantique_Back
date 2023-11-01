package com.example.socialmediawebsemantique;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class ReclamationModel {
    @Id
    private Integer id;
    private String title;
    private String description;
    private String date;
}