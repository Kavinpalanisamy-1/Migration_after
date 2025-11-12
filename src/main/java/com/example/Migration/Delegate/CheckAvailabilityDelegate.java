package com.example.Migration.Delegate;

import java.util.HashMap;
import java.util.Map;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.Migration.dao.InventoryDAO;

@Component("checkAvailabilityDelegate")
public class CheckAvailabilityDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(CheckAvailabilityDelegate.class);
    private final InventoryDAO inventoryDAO;

    @Autowired
    public CheckAvailabilityDelegate(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
        logger.info("✅ CheckAvailabilityDelegate bean created successfully");
    }

    @JobWorker(type = "checkAvailabilityDelegate", autoComplete = true)
    public Map<String, Object> executeJobMigrated(ActivatedJob job) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // ✅ FIXED: Handle Integer/Long conversion
            Object productIdObj = job.getVariable("selectedProductId");
            Long productId = null;
            
            if (productIdObj instanceof Integer) {
                productId = ((Integer) productIdObj).longValue();
                logger.debug("🔧 Converted Integer {} to Long {}", productIdObj, productId);
            } else if (productIdObj instanceof Long) {
                productId = (Long) productIdObj;
            } else if (productIdObj != null) {
                productId = Long.parseLong(productIdObj.toString());
            }
            
            String productName = (String) job.getVariable("selectedProductName");
            
            logger.info("🔍 CheckAvailabilityDelegate - Starting execution");
            logger.info("📦 Checking availability for Product ID: {}, Name: {}", productId, productName);
            
            if (productId == null) {
                logger.warn("⚠️ Product ID is null - marking as not available");
                resultMap.put("availability", "PRODUCT_NOT_FOUND");
                // ✅ FIX: Don't set proposalMade here - let ProposeAlternativesDelegate handle it
                resultMap.put("errorMessage", "Product ID is required");
                return;
            }

            logger.debug("🔍 Querying inventory for product ID: {}", productId);
            boolean available = inventoryDAO.isAvailable(productId);
            logger.info("📊 Availability result - Product ID: {}, Available: {}", productId, available);

            if (available) {
                resultMap.put("availability", "AVAILABLE");
                resultMap.put("proposalMade", false); // No proposal needed if available
                resultMap.put("errorMessage", null);
                logger.info("✅ Product {} is available - proceeding with order", productId);
            } else {
                resultMap.put("availability", "NOT_AVAILABLE");
                // ✅ FIX: Don't set proposalMade to false - let ProposeAlternativesDelegate set it to true if alternatives found
                resultMap.put("errorMessage", "Product is out of stock");
                logger.info("❌ Product {} is not available - will trigger alternatives", productId);
            }
            
            logger.info("✅ CheckAvailabilityDelegate - Execution completed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error in CheckAvailabilityDelegate: {}", e.getMessage(), e);
            resultMap.put("availability", "ERROR");
            resultMap.put("proposalMade", false);
            resultMap.put("errorMessage", "Error checking availability: " + e.getMessage());
        }
        return resultMap;
    }
}