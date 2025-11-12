package com.example.Migration.controller;

import com.example.Migration.dto.CustomerSelectionDTO;
import com.example.Migration.service.CustomerSelectionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.camunda.client.CamundaClient;

@RestController
@RequestMapping("/api/customer")
public class CustomerSelectionController {

    @Autowired
    private CamundaClient camundaClient;

    @Autowired
    private CustomerSelectionService service;

    @PostMapping("/choose-products")
    public String chooseProducts(@RequestBody CustomerSelectionDTO dto) {
        // 1️⃣ Save the customer’s selected products
        service.saveCustomerSelection(dto);

        // 2️⃣ Start Camunda BPMN process (OrderProcess)
        camundaClient
            .newCreateInstanceCommand()
            .bpmnProcessId("OrderProcess")
            .latestVersion()
            .send()
            .join();

        return "Customer products saved and OrderProcess started successfully!";
    }
}
