package com.example.rawsource.repositories;

import com.example.rawsource.entities.Item;
import com.example.rawsource.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    Optional<Item> findItemById(UUID id);

    List<Item> findByOrder(Order order);

    @Query("SELECT DISTINCT i.order FROM Item i WHERE i.product.provider.id = :providerId")
    List<Order> findOrdersByProvider(@Param("providerId") UUID providerId);
}
