package com.example.Migration.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory")
public class Inventory {

  @Id
  private Long productId;

  private Integer quantity;

  @Column(precision = 19, scale = 2) // optional but recommended
  private BigDecimal price;          // ✅ java.math.BigDecimal

  private String model;

  // getters/setters
  public Long getProductId() { return productId; }
  public void setProductId(Long productId) { this.productId = productId; }

  public Integer getQuantity() { return quantity; }
  public void setQuantity(Integer quantity) { this.quantity = quantity; }

  public BigDecimal getPrice() { return price; }                 
  public void setPrice(BigDecimal price) { this.price = price; } 

  public String getModel() { return model; }
  public void setModel(String model) { this.model = model; }
}
