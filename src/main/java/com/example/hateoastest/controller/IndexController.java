package com.example.hateoastest.controller;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class IndexController {

    @GetMapping("/")
    public RepresentationModel index() {
        RepresentationModel resourceSupport = new RepresentationModel();
        //resourceSupport.add(linkTo(methodOn(IndexController.class).index()).withSelfRel());
       // resourceSupport.add(linkTo(methodOn(CustomersController.class).listCustomers()).withRel("customers"));
        resourceSupport.add(Link.of("http://localhost:8081/","index"));
        resourceSupport.add(Link.of("http://localhost:8081/customers","customers"));
        return resourceSupport;
    }
}
