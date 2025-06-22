package com.example.rawsource.repositories;

import com.example.rawsource.entities.Order;
import com.example.rawsource.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAll();

    List<Order> findByBuyer(User buyer);
}
