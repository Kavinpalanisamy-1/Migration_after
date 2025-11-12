package com.example.Migration.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.Migration.dao.OrderDAO;
import com.example.Migration.model.OrderItem;

@RestController
@RequestMapping("/api")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderDAO orderDAO;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @Autowired
    public OrderController(OrderDAO orderDAO, RuntimeService runtimeService, TaskService taskService) {
        this.orderDAO = orderDAO;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

   
    // ✅ Get items for an order
    @GetMapping("/orders/{orderId}/items")
    public List<Map<String, Object>> getItems(@PathVariable Long orderId) {
        try {
            List<OrderItem> items = orderDAO.findItems(orderId);
            return items.stream().map(i -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("itemId", i.getItemId());
                map.put("productId", i.getProductId());
                map.put("productName", i.getProductName());
                map.put("price", i.getPrice());
                return map;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ✅ Start process
    @PostMapping("/orders/{orderId}/start")
    public Map<String, Object> startProcess(@PathVariable Long orderId) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("orderId", orderId);
            ProcessInstance pi = runtimeService.startProcessInstanceByKey("order-process", vars);

            Map<String, Object> response = new HashMap<>();
            response.put("processInstanceId", pi.getId());
            response.put("message", "Process started successfully");
            response.put("orderId", orderId);
            return response;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ✅ Complete task
    @PostMapping("/task/complete")
    public Map<String, Object> completeTask(@RequestParam String processInstanceId,
                                            @RequestParam String taskDefinitionKey,
                                            @RequestBody Map<String, Object> vars) {
        try {
            Task task = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .taskDefinitionKey(taskDefinitionKey)
                    .singleResult();

            if (task == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");

            taskService.complete(task.getId(), vars);

            Map<String, Object> resp = new HashMap<>();
            resp.put("taskId", task.getId());
            resp.put("taskName", task.getName());
            resp.put("processInstanceId", processInstanceId);
            resp.put("message", "Task completed successfully");
            return resp;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

//    // ✅ Set process variables
//    @PostMapping("/process/variables")
//    public Map<String, Object> setProcessVariables(@RequestParam String processInstanceId,
//                                                   @RequestBody Map<String, Object> variables) {
//        try {
//            runtimeService.setVariables(processInstanceId, variables);
//            Map<String, Object> resp = new HashMap<>();
//            resp.put("processInstanceId", processInstanceId);
//            resp.put("variablesSet", variables.keySet());
//            resp.put("message", "Variables set successfully");
//            return resp;
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
//        }
//    }

    // ✅ Get active tasks
    @GetMapping("/process/tasks")
    public List<Map<String, Object>> getActiveTasks(@RequestParam String processInstanceId) {
        try {
            return taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .map(task -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("taskId", task.getId());
                        map.put("taskDefinitionKey", task.getTaskDefinitionKey());
                        map.put("name", task.getName());
                        map.put("assignee", task.getAssignee());
                        map.put("created", task.getCreateTime());
                        return map;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ✅ Get all process instances
    @GetMapping("/process/instances")
    public List<Map<String, Object>> getAllProcessInstances() {
        try {
            return runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey("order-process")
                    .list()
                    .stream()
                    .map(pi -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("processInstanceId", pi.getId());
                        map.put("processDefinitionId", pi.getProcessDefinitionId());
                        map.put("businessKey", pi.getBusinessKey());
                        map.put("ended", pi.isEnded());
                        return map;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ✅ Get alternative products (fixed path)
    @GetMapping("/process/{processInstanceId}/alternatives")
    public ResponseEntity<?> getAlternativeProducts(@PathVariable String processInstanceId) {
        try {
            Map<String, Object> vars = runtimeService.getVariables(processInstanceId);
            String availability = (String) vars.get("availability");
            Boolean showAlternativesTask = (Boolean) vars.get("showAlternativesTask");
            List<Map<String, Object>> alternativeOptions = (List<Map<String, Object>>) vars.get("alternativeOptions");

            Map<String, Object> resp = new HashMap<>();

            if ("NOT_AVAILABLE".equals(availability) && Boolean.TRUE.equals(showAlternativesTask)) {
                resp.put("status", "ALTERNATIVES_AVAILABLE");
                resp.put("message", "Original product not available. Please choose an alternative.");
                resp.put("alternatives", alternativeOptions != null ? alternativeOptions : List.of());
                resp.put("originalProductId", vars.get("selectedProductId"));
                resp.put("originalProductName", vars.get("selectedProductName"));
            } else if ("NOT_AVAILABLE".equals(availability)) {
                resp.put("status", "NO_ALTERNATIVES");
                resp.put("message", "Original product not available and no alternatives found.");
                resp.put("alternatives", List.of());
            } else {
                resp.put("status", "PRODUCT_AVAILABLE");
                resp.put("message", "Original product is available.");
            }

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Choose alternative product (fixed path)
    @PostMapping("/process/{processInstanceId}/choose-alternative")
    public ResponseEntity<?> chooseAlternativeProduct(@PathVariable String processInstanceId,
                                                      @RequestBody Map<String, Object> req) {
        try {
            Long chosenAlternativeId = Long.valueOf(req.get("chosenAlternativeId").toString());
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .taskDefinitionKey("chooseAlternative")
                    .list();

            if (tasks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No 'chooseAlternative' task found for this process"));
            }

            Task task = tasks.get(0);
            Map<String, Object> vars = new HashMap<>();
            vars.put("chosenAlternativeId", chosenAlternativeId);

            taskService.complete(task.getId(), vars);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Alternative chosen successfully",
                    "chosenAlternativeId", chosenAlternativeId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
