package com.example.Migration.service;

import com.example.Migration.dao.CustomerSelectionDAO;
import com.example.Migration.dto.CustomerSelectionDTO;
import com.example.Migration.model.CustomerSelection;
import com.example.Migration.model.SelectedProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerSelectionService {

    @Autowired
    private CustomerSelectionDAO customerSelectionDAO;

    public void saveCustomerSelection(CustomerSelectionDTO dto) {
        CustomerSelection selection = new CustomerSelection();

        List<SelectedProduct> products = new ArrayList<>();
        for (CustomerSelectionDTO.ProductDTO p : dto.getSelectedProducts()) {
            SelectedProduct sp = new SelectedProduct();
            sp.setProductId(p.getProductId());
            sp.setQuantity(p.getQuantity());
            sp.setCustomerSelection(selection);
            products.add(sp);
        }

        selection.setSelectedProducts(products);
        customerSelectionDAO.saveCustomerSelection(selection);
    }
}
