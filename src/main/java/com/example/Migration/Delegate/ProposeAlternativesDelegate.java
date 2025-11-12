package com.example.Migration.Delegate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.Migration.dao.InventoryDAO;
import com.example.Migration.model.Inventory;

@Component("proposeAlternativesDelegate")
public class ProposeAlternativesDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ProposeAlternativesDelegate.class);
    private final InventoryDAO inventoryDAO;

    @Autowired
    public ProposeAlternativesDelegate(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
        logger.info("✅ ProposeAlternativesDelegate bean created successfully");
    }

    @JobWorker(type = "proposeAlternativesDelegate", autoComplete = true)
    public Map<String, Object> executeJobMigrated(ActivatedJob job) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String productName = (String) job.getVariable("selectedProductName");
            
            // ✅ FIXED: Handle Integer/Long conversion for productId
            Object productIdObj = job.getVariable("selectedProductId");
            Long currentProductId = null;
            
            if (productIdObj instanceof Integer) {
                currentProductId = ((Integer) productIdObj).longValue();
                logger.debug("🔧 Converted Integer {} to Long {}", productIdObj, currentProductId);
            } else if (productIdObj instanceof Long) {
                currentProductId = (Long) productIdObj;
            } else if (productIdObj != null) {
                currentProductId = Long.parseLong(productIdObj.toString());
            }
            
            logger.info("🔍 ProposeAlternativesDelegate - Starting execution");
            logger.info("🔄 Finding alternatives for Product ID: {}, Name: {}", currentProductId, productName);
            
            if (currentProductId == null) {
                logger.error("❌ Current product ID is null - cannot find alternatives");
                resultMap.put("proposalMade", false);
                resultMap.put("errorMessage", "Current product ID is required");
                return;
            }
            
            String modelPrefix = "Item"; // Default prefix
            if (productName != null && !productName.trim().isEmpty()) {
                String[] nameParts = productName.split(" ");
                modelPrefix = nameParts.length > 0 ? nameParts[0] : "Item";
                logger.debug("🔍 Extracted model prefix: {} from product name: {}", modelPrefix, productName);
            } else {
                logger.warn("⚠️ Product name is null or empty, using default prefix: Item");
            }

            logger.info("🔍 Searching alternatives with model prefix: {}", modelPrefix);
            List<Inventory> alternatives = inventoryDAO.findAlternativesByModelPrefix(modelPrefix);
            
            // ✅ FIX: Create a final copy of currentProductId for use in lambda
            final Long finalCurrentProductId = currentProductId;
            
            // Filter out the current product from alternatives
            List<Inventory> filteredAlternatives = alternatives.stream()
                .filter(alt -> !alt.getProductId().equals(finalCurrentProductId))
                .collect(Collectors.toList());

            logger.info("📊 Found {} total alternatives, {} after filtering out current product", 
                       alternatives.size(), filteredAlternatives.size());

            // ✅ CHANGED: Always set proposalMade to true if alternatives found
            // This will create a human task for user to choose alternatives
            if (filteredAlternatives.isEmpty()) {
                resultMap.put("proposalMade", false);
                resultMap.put("alternativeOptions", null);
                resultMap.put("alternativesCount", 0);
                resultMap.put("showAlternativesTask", false);
                logger.warn("⚠️ No alternatives found for product {} with prefix {}", currentProductId, modelPrefix);
            } else {
                // ✅ CHANGED: Set variables to trigger human task
                resultMap.put("proposalMade", true);
                resultMap.put("showAlternativesTask", true);
                resultMap.put("alternativesAvailable", true);
                
                // Create list of alternative maps for API response
                List<Map<String, Object>> alternativeList = filteredAlternatives.stream()
                    .map(alt -> {
                        Map<String, Object> altMap = new HashMap<>();
                        altMap.put("productId", alt.getProductId());
                        altMap.put("model", alt.getModel());
                        altMap.put("price", alt.getPrice());
                        altMap.put("quantity", alt.getQuantity());
                        altMap.put("available", alt.getQuantity() > 0);
                        return altMap;
                    })
                    .collect(Collectors.toList());

                resultMap.put("alternativeOptions", alternativeList);
                resultMap.put("alternativesCount", filteredAlternatives.size());
                
                logger.info("✅ Proposed {} alternatives for product {}", filteredAlternatives.size(), currentProductId);
                logger.info("👤 Human task will be created for user to choose alternative");
                logger.debug("📋 Alternative products: {}", alternativeList);
            }
            
            logger.info("✅ ProposeAlternativesDelegate - Execution completed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error in ProposeAlternativesDelegate: {}", e.getMessage(), e);
            resultMap.put("proposalMade", false);
            resultMap.put("showAlternativesTask", false);
            resultMap.put("errorMessage", "Error proposing alternatives: " + e.getMessage());
        }
        return resultMap;
    }
}