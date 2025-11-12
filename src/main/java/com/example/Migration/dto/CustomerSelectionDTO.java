package com.example.Migration.dto;

import java.util.List;
import java.util.ArrayList;

public class CustomerSelectionDTO {
    private List<ProductDTO> selectedProducts = new ArrayList<>();

    public static class ProductDTO {
        private String productId;
        private int quantity;

        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public List<ProductDTO> getSelectedProducts() { return selectedProducts; }
    public void setSelectedProducts(List<ProductDTO> selectedProducts) { 
        this.selectedProducts = selectedProducts != null ? selectedProducts : new ArrayList<>();
    }
}