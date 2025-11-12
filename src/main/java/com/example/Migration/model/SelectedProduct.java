package com.example.Migration.model;

import javax.persistence.*;

@Entity
@Table(name = "selected_product")
public class SelectedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productId;
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerSelection customerSelection;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public CustomerSelection getCustomerSelection() { return customerSelection; }
    public void setCustomerSelection(CustomerSelection customerSelection) { this.customerSelection = customerSelection; }
}
