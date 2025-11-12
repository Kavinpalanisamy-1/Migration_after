package com.example.Migration.Delegate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.Migration.dao.InventoryDAO;

@Component("evaluateProposalDelegate")
public class EvaluateProposalDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(EvaluateProposalDelegate.class);
    private final InventoryDAO inventoryDAO;

    @Autowired
    public EvaluateProposalDelegate(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
        logger.info("✅ EvaluateProposalDelegate bean created successfully");
    }
    
    // ✅ Helper method for Integer/Long conversion
    private Long convertToLong(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj != null) {
            try {
                return Long.parseLong(obj.toString());
            } catch (NumberFormatException e) {
                logger.warn("⚠️ Cannot convert {} to Long: {}", obj, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @JobWorker(type = "evaluateProposalDelegate", autoComplete = true)
    public Map<String, Object> executeJobMigrated(ActivatedJob job) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // ✅ FIXED: Handle Integer/Long conversion for both IDs
            Object originalProductIdObj = job.getVariable("selectedProductId");
            Object chosenAlternativeIdObj = job.getVariable("chosenAlternativeId");
            
            Long originalProductId = convertToLong(originalProductIdObj);
            Long chosenAlternativeId = convertToLong(chosenAlternativeIdObj);
            
            logger.info("🔍 EvaluateProposalDelegate - Starting execution");
            logger.info("📊 Evaluating proposal - Original Product ID: {}, Chosen Alternative ID: {}", 
                       originalProductId, chosenAlternativeId);
            
            // Validate inputs
            if (originalProductId == null) {
                logger.error("❌ Original product ID is null - cannot evaluate proposal");
                resultMap.put("approved", false);
                resultMap.put("errorMessage", "Original product ID is required");
                return;
            }

            logger.debug("🔍 Getting price for original product ID: {}", originalProductId);
            Optional<BigDecimal> originalPriceOpt = inventoryDAO.getCurrentPrice(originalProductId);
            if (originalPriceOpt.isEmpty()) {
                logger.error("❌ Original product not found - ID: {}", originalProductId);
                resultMap.put("approved", false);
                resultMap.put("errorMessage", "Original product not found");
                return;
            }

            BigDecimal oldPrice = originalPriceOpt.get();
            BigDecimal newPrice = oldPrice;
            Long finalProductId = originalProductId;

            // If alternative is chosen, get its price
            if (chosenAlternativeId != null) {
                logger.debug("🔍 Getting price for alternative product ID: {}", chosenAlternativeId);
                Optional<BigDecimal> newPriceOpt = inventoryDAO.getCurrentPrice(chosenAlternativeId);
                if (newPriceOpt.isEmpty()) {
                    logger.error("❌ Alternative product not found - ID: {}", chosenAlternativeId);
                    resultMap.put("approved", false);
                    resultMap.put("errorMessage", "Alternative product not found");
                    return;
                }
                newPrice = newPriceOpt.get();
                finalProductId = chosenAlternativeId;
                logger.info("🔄 Alternative chosen - Original: {} (₹{}), Alternative: {} (₹{})", 
                           originalProductId, oldPrice, chosenAlternativeId, newPrice);
            } else {
                logger.info("📊 No alternative chosen - using original product: {} (₹{})", originalProductId, oldPrice);
            }

            // Calculate price difference
            BigDecimal priceDifference = newPrice.subtract(oldPrice);
            BigDecimal amountDue = priceDifference.compareTo(BigDecimal.ZERO) > 0 ? priceDifference : BigDecimal.ZERO;
            
            logger.info("💰 Price calculation - Old: ₹{}, New: ₹{}, Difference: ₹{}, Amount Due: ₹{}", 
                       oldPrice, newPrice, priceDifference, amountDue);

            // Approval logic: approve if price difference is <= 20% of original price
            boolean approved = priceDifference.compareTo(oldPrice.multiply(new BigDecimal("0.2"))) <= 0;

            resultMap.put("amountDue", amountDue);
            resultMap.put("approved", approved);
            resultMap.put("originalPrice", oldPrice);
            resultMap.put("newPrice", newPrice);
            resultMap.put("priceDifference", priceDifference);
            resultMap.put("finalProductId", finalProductId);
            resultMap.put("errorMessage", null);

            if (approved) {
                logger.info("✅ Proposal APPROVED - Amount due: ₹{}, Final product: {}", amountDue, finalProductId);
            } else {
                logger.warn("❌ Proposal REJECTED - Price difference ₹{} exceeds 20% threshold", priceDifference);
            }
            
            logger.info("✅ EvaluateProposalDelegate - Execution completed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error in EvaluateProposalDelegate: {}", e.getMessage(), e);
            resultMap.put("approved", false);
            resultMap.put("errorMessage", "Error evaluating proposal: " + e.getMessage());
        }
        return resultMap;
    }
}