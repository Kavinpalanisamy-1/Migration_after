package com.example.Migration.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "customer_selection")
public class CustomerSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @OneToMany(mappedBy = "customerSelection", cascade = CascadeType.ALL)
    private List<SelectedProduct> selectedProducts;

    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<SelectedProduct> getSelectedProducts() { return selectedProducts; }
    public void setSelectedProducts(List<SelectedProduct> selectedProducts) { this.selectedProducts = selectedProducts; }
}
