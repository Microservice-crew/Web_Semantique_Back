package com.example.socialmediawebsemantique;

import com.example.socialmediawebsemantique.tools.jenaEngine;
import org.apache.jena.rdf.model.Model;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class controller {

    Model model = jenaEngine.readModel("data/oneZero.owl");




}
