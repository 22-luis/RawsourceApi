package com.example.rawsource.repositories;

import com.example.rawsource.entities.Inventory;
import com.example.rawsource.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByUser(User user);
    
    Optional<Inventory> findByUserId(UUID userId);
    
    boolean existsByUser(User user);
    
}