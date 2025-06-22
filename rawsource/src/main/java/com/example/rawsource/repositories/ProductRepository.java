package com.example.rawsource.repositories;

import com.example.rawsource.entities.Product;
import com.example.rawsource.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findProductById(UUID id);
    List<Product> findByProvider(User provider);
}
