package com.example.Migration.dao;

import com.example.Migration.model.CustomerSelection;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerSelectionDAO {

    @Autowired
    private SessionFactory sessionFactory;

    public void saveCustomerSelection(CustomerSelection selection) {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(selection);
        tx.commit();
        session.close();
    }

    public CustomerSelection getCustomerSelectionById(Long id) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CustomerSelection> cq = cb.createQuery(CustomerSelection.class);
        Root<CustomerSelection> root = cq.from(CustomerSelection.class);
        cq.select(root).where(cb.equal(root.get("customerId"), id));

        Query<CustomerSelection> query = session.createQuery(cq);
        CustomerSelection result = query.uniqueResult();
        session.close();
        return result;
    }
}
