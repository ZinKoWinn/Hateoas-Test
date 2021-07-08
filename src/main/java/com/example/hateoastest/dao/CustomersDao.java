package com.example.hateoastest.dao;


import com.example.hateoastest.ds.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomersDao extends CrudRepository<Customer, Integer> {
}
