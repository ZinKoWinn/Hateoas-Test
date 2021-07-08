package com.example.hateoastest.dao;


import com.example.hateoastest.ds.Address;
import org.springframework.data.repository.CrudRepository;

public interface AddressesDao extends CrudRepository<Address, Integer> {
}
