package com.example.Migration.Delegate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.spring.client.annotation.JobWorker;
import io.camunda.spring.client.exception.CamundaError;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("processPaymentDelegate")
public class ProcessPaymentDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessPaymentDelegate.class);
    
    public ProcessPaymentDelegate() {
        logger.info("✅ ProcessPaymentDelegate bean created successfully");
    }

    @JobWorker(type = "processPaymentDelegate", autoComplete = true)
    public Map<String, Object> executeJobMigrated(ActivatedJob job) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            BigDecimal amountDue = (BigDecimal) job.getVariable("amountDue");
            
            logger.info("🔍 ProcessPaymentDelegate - Starting execution");
            logger.info("💳 Processing payment - Amount due: ₹{}", amountDue);
            
            // If no amount due or zero amount, payment is successful
            if (amountDue == null || BigDecimal.ZERO.compareTo(amountDue) == 0) {
                resultMap.put("paymentStatus", "SUCCESS");
                resultMap.put("paymentMessage", "No payment required");
                logger.info("✅ Payment successful - No amount due");
                return;
            }

            // Simulate payment processing with 80% success rate
            boolean success = Math.random() > 0.2;
            logger.debug("🎲 Payment simulation - Success rate: 80%, Result: {}", success);

            if (success) {
                resultMap.put("paymentStatus", "SUCCESS");
                resultMap.put("paymentMessage", "Payment processed successfully");
                logger.info("✅ Payment SUCCESSFUL - Amount: ₹{}", amountDue);
            } else {
                resultMap.put("paymentStatus", "FAILED");
                resultMap.put("paymentMessage", "Payment gateway declined the transaction");
                logger.error("❌ Payment FAILED - Amount: ₹{}", amountDue);
                throw CamundaError.bpmnError("PAYMENT_FAILED", "Payment processing failed");
            }
            
            logger.info("✅ ProcessPaymentDelegate - Execution completed successfully");
            
        } catch (BpmnError e) {
            logger.error("❌ BPMN Error in ProcessPaymentDelegate: {}", e.getMessage());
            throw e; // Re-throw BPMN errors
        } catch (Exception e) {
            logger.error("❌ Unexpected error in ProcessPaymentDelegate: {}", e.getMessage(), e);
            throw CamundaError.bpmnError("PAYMENT_FAILED", "Unexpected error during payment: " + e.getMessage());
        }
        return resultMap;
    }
}