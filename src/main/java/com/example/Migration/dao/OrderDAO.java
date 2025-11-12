package com.example.Migration.dao;

import com.example.Migration.model.OrderItem;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderDAO {
  @PersistenceContext
  private EntityManager em;

  public List<OrderItem> findItems(Long orderId) {
    return em.createQuery(
        "select oi from OrderItem oi where oi.orderId = :orderId order by oi.itemId",
        OrderItem.class)
      .setParameter("orderId", orderId)
      .getResultList();
  }
}
