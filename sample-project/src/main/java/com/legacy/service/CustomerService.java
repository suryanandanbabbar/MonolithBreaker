package com.legacy.service;
import com.legacy.model.Customer; import com.legacy.repository.CustomerRepository; import com.legacy.util.Notifier;
@Deprecated
public class CustomerService { private final CustomerRepository repo; private final Notifier notifier = new Notifier(); public CustomerService(CustomerRepository repo){this.repo=repo;} public Customer get(Long id){ var c=repo.findById(id); notifier.send(c.name); return c; }}
