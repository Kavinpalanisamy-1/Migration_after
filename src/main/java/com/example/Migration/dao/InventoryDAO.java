package com.example.Migration.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.Migration.model.Inventory;

@Repository
public class InventoryDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryDAO.class);
    
    @PersistenceContext
    private EntityManager em;

    public boolean isAvailable(Long productId) {
        logger.debug("🔍 Checking availability for product ID: {}", productId);
        Inventory inv = em.find(Inventory.class, productId);
        boolean available = inv != null && inv.getQuantity() != null && inv.getQuantity() > 0;
        logger.debug("📦 Availability result for product {}: {}", productId, available);
        return available;
    }

    public List<Inventory> findAlternativesByModelPrefix(String modelPrefix) {
        logger.debug("🔍 Finding alternatives with model prefix: {}", modelPrefix);
        List<Inventory> results = em.createQuery(
            "SELECT i FROM Inventory i WHERE i.model LIKE :model AND i.quantity > 0 ORDER BY i.price", 
            Inventory.class)
        .setParameter("model", modelPrefix + "%")
        .setMaxResults(5)
        .getResultList();
        logger.debug("📊 Found {} alternatives for prefix: {}", results.size(), modelPrefix);
        return results;
    }

    public Optional<BigDecimal> getCurrentPrice(Long productId) {
        logger.debug("🔍 Getting current price for product ID: {}", productId);
        Inventory inv = em.find(Inventory.class, productId);
        BigDecimal price = inv != null ? inv.getPrice() : null;
        logger.debug("💰 Price for product {}: {}", productId, price);
        return Optional.ofNullable(price);
    }

    public Optional<Inventory> findById(Long productId) {
        logger.debug("🔍 Finding inventory by product ID: {}", productId);
        Inventory inventory = em.find(Inventory.class, productId);
        return Optional.ofNullable(inventory);
    }
}