package com.example.rawsource.repositories;

import com.example.rawsource.entities.Inventory;
import com.example.rawsource.entities.InventoryProduct;
import com.example.rawsource.entities.Product;
import com.example.rawsource.entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryProductRepository extends JpaRepository<InventoryProduct, UUID> {

    List<InventoryProduct> findByInventory(Inventory inventory);
    
    Optional<InventoryProduct> findByInventoryAndProduct(Inventory inventory, Product product);
    
    List<InventoryProduct> findByInventoryAndStatus(Inventory inventory, Status status);
    
    List<InventoryProduct> findByInventoryAndQuantityLessThanEqual(Inventory inventory, Integer quantity);
    
    boolean existsByInventoryAndProduct(Inventory inventory, Product product);
}
