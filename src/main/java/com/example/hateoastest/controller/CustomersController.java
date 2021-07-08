package com.example.hateoastest.controller;


import com.example.hateoastest.dao.AddressesDao;
import com.example.hateoastest.dao.CustomersDao;
import com.example.hateoastest.ds.Address;
import com.example.hateoastest.ds.Customer;
import org.hibernate.bytecode.enhance.internal.bytebuddy.EnhancerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
public class CustomersController {

    public static final Class<CustomersController> CONTROLLER_CLASS = CustomersController.class;
    @Autowired
    private CustomersDao customersDao;
    @Autowired
    private AddressesDao addressesDao;

    @GetMapping("/customers/{id}")
    public EntityModel<Customer> getCustomer(@PathVariable int id){
        Class<CustomersController>  controllerClass=CustomersController.class;
        Optional<Customer> customer=customersDao.findById(id);
        if(!customer.isPresent()){
            throw new EntityNotFoundException("Id-"+ id);
        }
        EntityModel<Customer> resource =EntityModel.of(customer.get());
        resource.add(linkTo(methodOn(CONTROLLER_CLASS).getCustomer(id)).withSelfRel());
        resource.add(linkTo(methodOn(CONTROLLER_CLASS).getCustomer(id)).withRel("customer"));
        resource.add(linkTo(methodOn(CONTROLLER_CLASS).listAddresses(id)).withRel("addresses"));

        return resource;
    }
    @GetMapping("/customers")
    public CollectionModel<EntityModel<Customer>> listCustomers(){
        List<EntityModel<Customer>> customerEntityModel=StreamSupport.stream(customersDao.findAll().spliterator(),false)
                .map(cus -> EntityModel.of(cus,linkTo(methodOn(CONTROLLER_CLASS).getCustomer(cus.getId())).withSelfRel()
                ,linkTo(methodOn(CONTROLLER_CLASS).getCustomer(cus.getId())).withRel("customer"),
                        linkTo(methodOn(CONTROLLER_CLASS).listAddresses(cus.getId())).withRel("addresses"),linkTo(methodOn(CONTROLLER_CLASS).listCustomers()).withRel("customers"))
        ).collect(Collectors.toList());
        Link customersLink=linkTo(methodOn(CONTROLLER_CLASS).listCustomers()).withSelfRel();
        return CollectionModel.of(customerEntityModel,customersLink);
    }
    @GetMapping("/customers/{customerId}/addresses/{addressId}")
    public EntityModel<Address> getAddress(@PathVariable int customerId,@PathVariable int addressId){
        Customer customer=customersDao.findById(customerId)
                .orElseThrow(()-> new ResponseStatusException(NOT_FOUND));
        Address customerAddress=customer.getAddresses().stream()
                .filter(address -> address.getId().equals(addressId))
                .findAny()
                .orElseThrow(()-> new ResponseStatusException(NOT_FOUND));
        return EntityModel.of(customerAddress,linkTo(methodOn(CONTROLLER_CLASS).getAddress(customerId,addressId)).withSelfRel(),
                linkTo(methodOn(CONTROLLER_CLASS).getCustomer(customerId)).withRel("customer"), linkTo(methodOn(CONTROLLER_CLASS).listCustomers()).withRel("customers"));
    }
    @GetMapping("/customers/{customerId}/addresses")
    public CollectionModel<EntityModel<Address>> listAddresses(@PathVariable int customerId){
        Customer customer=customersDao.findById(customerId).orElseThrow(()-> new ResponseStatusException(NOT_FOUND));
        List<EntityModel<Address>> addresses=customer.getAddresses().stream()
                .map(address -> EntityModel.of(address,linkTo(methodOn(CONTROLLER_CLASS).getAddress(customerId,address.getId())).withSelfRel(),
                        linkTo(methodOn(CONTROLLER_CLASS).getCustomer(address.getCustomer().getId())).withRel("customer"), linkTo(methodOn(CONTROLLER_CLASS).listCustomers()).withRel("customers")))
                .collect(Collectors.toList());
        return CollectionModel.of(addresses);
    }
// curl -X POST -H 'Content-Type: application/json' -d '{"code":"CS","firstName":"Zin Ko","lastName":"Winn"}' http://localhost:8080/customers
    @PostMapping("/customers")
    public EntityModel<Customer> createCustomer(@RequestBody Customer customer){
        Customer createdCustomer = customersDao.save(customer);
        return EntityModel.of(createdCustomer,linkTo(methodOn(CONTROLLER_CLASS).getCustomer(customer.getId())).withSelfRel());
    }

    // curl -X POST -H 'Content-Type: application/json' -d '{"addressName":"Apartment Address", "streetName":"Aung San","streetNumber":"2345",""}'
    @PostMapping("/customers/{id}/addresses")
    public EntityModel<Address> createAddress(@PathVariable int id, @RequestBody Address address){
        Customer customer = customersDao.findById(id).orElseThrow();
        address.setCustomer(customer);
        address = addressesDao.save(address);
        customer.getAddresses().add(address);
        customer = customersDao.save(customer);

        return EntityModel.of(address,linkTo(methodOn(CONTROLLER_CLASS).getAddress(customer.getId(),address.getId())).withSelfRel());
    }




    /*
     @GetMapping("/customers/{id}/addresses")
    public Resources<Resource<Address>> listAddresses(@PathVariable int id) {
        Customer customer = customersDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        List<Resource<Address>> addresses = customer.getAddresses().stream()
                .map(address -> new Resource<>(address,
                        linkTo(methodOn(CustomersController.class).getAddress(id, address.getId())).withSelfRel(),
                        linkTo(methodOn(CustomersController.class).getCustomer(address.getCustomer().getId())).withRel("customer"))
                )
                .collect(Collectors.toList());

        return new Resources<>(addresses);
    }
     */

    /*
    @GetMapping("/customers/{customerId}/addresses/{addressId}")
    public Resource<Address> getAddress(@PathVariable int customerId, @PathVariable int addressId) {
        Customer customer = customersDao.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        Address customerAddress = customer.getAddresses().stream()
                .filter(address -> address.getId().equals(addressId))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        return new Resource<>(
                customerAddress,
                linkTo(methodOn(CustomersController.class).getAddress(customerId, customerAddress.getId())).withSelfRel(),
                linkTo(methodOn(CustomersController.class).listAddresses(customerId)).withRel("addresses"),
                linkTo(methodOn(CustomersController.class).getCustomer(customerId)).withRel("customer")
        );
    }
     */


    /*
     @GetMapping("/customers/{customerId}/addresses/{addressId}")
    public Resource<Address> getAddress(@PathVariable int customerId, @PathVariable int addressId) {
        Customer customer = customersDao.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        Address customerAddress = customer.getAddresses().stream()
                .filter(address -> address.getId().equals(addressId))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        return new Resource<>(
                customerAddress,
                linkTo(methodOn(CustomersController.class).getAddress(customerId, customerAddress.getId())).withSelfRel(),
                linkTo(methodOn(CustomersController.class).listAddresses(customerId)).withRel("addresses"),
                linkTo(methodOn(CustomersController.class).getCustomer(customerId)).withRel("customer")
        );
    }
     */
   /* @GetMapping("/customers")
    public CollectionModel<EntityModel<Customer>> listCustomers() {
        List<EntityModel<Customer>> customers = StreamSupport.stream(customersDao.findAll().spliterator(), false)
                .map(customer -> new EntityModel(customer, linkTo(methodOn(CustomersController.class).getCustomer(customer.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return new CollectionModel<>(
                customers,
                linkTo(methodOn(IndexController.class).index()).withRel("index")
        );
    }

    @PostMapping("/customers")
    public Resource<Customer> createCustomer(@RequestBody @Valid Customer customer) {
        Customer createdCustomer = customersDao.save(customer);

        return new Resource<>(
                createdCustomer,
                linkTo(methodOn(CustomersController.class).getCustomer(customer.getId())).withSelfRel()
        );
    }

    @PutMapping("/customers")
    public Resources<Resource<Customer>> updateCustomers(@RequestBody @Valid Collection<Customer> customers) {
        customersDao.deleteAll();
        Iterable<Customer> updatedCustomers = customersDao.saveAll(customers);

        List<Resource<Customer>> customerResources = StreamSupport.stream(updatedCustomers.spliterator(), false)
                .map(customer -> new Resource<>(customer, linkTo(methodOn(CustomersController.class).getCustomer(customer.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return new Resources<>(
                customerResources,
                linkTo(methodOn(CustomersController.class).listCustomers()).withRel("customers")
        );
    }

    @DeleteMapping("/customers")
    public ResponseEntity deleteCustomers() {
        if (customersDao.count() > 0) {
            customersDao.deleteAll();
            return new ResponseEntity(NO_CONTENT);
        } else
            return new ResponseEntity(NOT_FOUND);
    }

    @GetMapping("/customers/{id}")
    public Resource<Customer> getCustomer(@PathVariable int id) {
        Customer customer = customersDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        return new Resource<>(
                customer,
                linkTo(methodOn(CustomersController.class).getCustomer(customer.getId())).withSelfRel(),
                linkTo(methodOn(CustomersController.class).listAddresses(id)).withRel("addresses"),
                linkTo(methodOn(CustomersController.class).listCustomers()).withRel("customers")
        );
    }

    @PutMapping("/customers/{id}")
    public Resource<Customer> updateCustomer(@PathVariable int id, @RequestBody @Valid Customer customer) {

        customer.setId(id);
        Customer savedCustomer = customersDao.save(customer);

        return new Resource<>(
                savedCustomer,
                linkTo(methodOn(CustomersController.class).getCustomer(customer.getId())).withSelfRel(),
                linkTo(methodOn(CustomersController.class).listCustomers()).withRel("customers")
        );
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity deleteCustomer(@PathVariable int id) {
        if (customersDao.existsById(id)) {
            customersDao.deleteById(id);
            return new ResponseEntity(NO_CONTENT);
        } else
            return new ResponseEntity(NOT_FOUND);
    }

    @GetMapping("/customers/{id}/addresses")
    public Resources<Resource<Address>> listAddresses(@PathVariable int id) {
        Customer customer = customersDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        List<Resource<Address>> addresses = customer.getAddresses().stream()
                .map(address -> new Resource<>(address,
                        linkTo(methodOn(CustomersController.class).getAddress(id, address.getId())).withSelfRel(),
                        linkTo(methodOn(CustomersController.class).getCustomer(address.getCustomer().getId())).withRel("customer"))
                )
                .collect(Collectors.toList());

        return new Resources<>(addresses);
    }

    @PostMapping("/customers/{id}/addresses")
    public Resource<Address> createAddress(@PathVariable int id, @RequestBody @Valid Address address) {
        Customer customer = customersDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        address.setCustomer(customer);
        address = addressesDao.save(address);

        customer.getAddresses().add(address);

        customer = customersDao.save(customer);

        return new Resource<>(
                address,
                linkTo(methodOn(CustomersController.class).getAddress(customer.getId(), address.getId())).withSelfRel()
        );
    }

    @PutMapping("/customers/{id}/addresses")
    public Resources<Resource<Address>> updateAddresses(@PathVariable int id, @RequestBody @Valid List<Address> addresses) {
        Customer customer = customersDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        List<Address> currentForCustomer = customer.getAddresses();
        currentForCustomer.forEach(address -> address.setCustomer(null));
        addressesDao.deleteAll(currentForCustomer);

        addresses.forEach(address -> address.setCustomer(customer));
        Iterable<Address> savedAddresses = addressesDao.saveAll(addresses);

        List<Resource<Address>> resources = StreamSupport.stream(savedAddresses.spliterator(), false)
                .map(address -> new Resource<>(address, linkTo(methodOn(CustomersController.class).getAddress(id, address.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return new Resources<>(
                resources,
                linkTo(methodOn(CustomersController.class).getCustomer(id)).withRel("customer")
        );
    }

    @DeleteMapping("/customers/{customerId}/addresses")
    public ResponseEntity deleteAddresses(@PathVariable int customerId) {
        Customer customer = customersDao.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        List<Address> addresses = customer.getAddresses();
        if (addresses.isEmpty())
            throw new ResponseStatusException(NOT_FOUND);

        addresses.forEach(address -> address.setCustomer(null));
        addressesDao.deleteAll(addresses);

        return new ResponseEntity(NO_CONTENT);
    }

    @GetMapping("/customers/{customerId}/addresses/{addressId}")
    public Resource<Address> getAddress(@PathVariable int customerId, @PathVariable int addressId) {
        Customer customer = customersDao.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        Address customerAddress = customer.getAddresses().stream()
                .filter(address -> address.getId().equals(addressId))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        return new Resource<>(
                customerAddress,
                linkTo(methodOn(CustomersController.class).getAddress(customerId, customerAddress.getId())).withSelfRel(),
                linkTo(methodOn(CustomersController.class).listAddresses(customerId)).withRel("addresses"),
                linkTo(methodOn(CustomersController.class).getCustomer(customerId)).withRel("customer")
        );
    }

    @PutMapping("/customers/{customerId}/addresses/{addressId}")
    public Resource<Address> updateAddress(@PathVariable int customerId, @PathVariable int addressId, @RequestBody @Valid Address address) {
        Customer customer = customersDao.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        if (!addressesDao.existsById(addressId))
            throw new ResponseStatusException(NOT_FOUND);

        address.setId(addressId);
        address.setCustomer(customer);

        Address savedAddress = addressesDao.save(address);

        return new Resource<>(
                savedAddress,
                linkTo(methodOn(CustomersController.class).getAddress(customerId, savedAddress.getId())).withSelfRel(),
                linkTo(methodOn(CustomersController.class).getCustomer(customerId)).withRel("customer")
        );
    }

    @DeleteMapping("/customers/{customerId}/addresses/{addressId}")
    public ResponseEntity deleteAddress(@PathVariable int customerId, @PathVariable int addressId) {
        if (!customersDao.existsById(customerId))
            throw new ResponseStatusException(NOT_FOUND);

        Address address = addressesDao.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        address.setCustomer(null);
        addressesDao.delete(address);

        return new ResponseEntity(NO_CONTENT);
    }

    */
}
