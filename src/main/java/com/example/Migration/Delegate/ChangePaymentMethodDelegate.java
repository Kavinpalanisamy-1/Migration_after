package com.example.Migration.Delegate;

import java.util.HashMap;
import java.util.Map;

import io.camunda.client.api.response.ActivatedJob;
import io.camunda.spring.client.annotation.JobWorker;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("changePaymentMethodDelegate")
public class ChangePaymentMethodDelegate {
    
    private static final Logger logger = LoggerFactory.getLogger(ChangePaymentMethodDelegate.class);
    
    public ChangePaymentMethodDelegate() {
        logger.info("✅ ChangePaymentMethodDelegate bean created successfully");
    }
    
    private int getRetryCount(DelegateExecution ex) {
        Object retryCount = job.getVariable("paymentRetryCount");
        return retryCount != null ? (Integer) retryCount : 0;
    }

    @JobWorker(type = "changePaymentMethodDelegate", autoComplete = true)
    public Map<String, Object> executeJobMigrated(ActivatedJob job) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            logger.info("🔍 ChangePaymentMethodDelegate - Starting execution");
            logger.info("🔄 Changing payment method after failed payment");

            resultMap.put("paymentMethodChanged", true);
            resultMap.put("paymentRetryCount", getRetryCount(ex) + 1);
            
            logger.info("✅ Payment method changed - Retry count: {}", getRetryCount(ex));
            logger.info("✅ ChangePaymentMethodDelegate - Execution completed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error in ChangePaymentMethodDelegate: {}", e.getMessage(), e);
            resultMap.put("paymentMethodChanged", false);
            resultMap.put("errorMessage", "Error changing payment method: " + e.getMessage());
        }
        return resultMap;
    }
}