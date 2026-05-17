package com.contract.service;

import cn.hutool.core.util.IdUtil;
import com.contract.entity.Contract;
import com.contract.entity.Supplier;
import com.contract.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    @Autowired
    private InMemoryStorage storage;

    public Supplier createSupplier(Supplier supplier) {
        supplier.setId(IdUtil.simpleUUID());
        supplier.setCreateTime(LocalDateTime.now());
        supplier.setUpdateTime(LocalDateTime.now());
        if (supplier.getScore() == null) {
            supplier.setScore(100);
        }
        if (supplier.getOverdueCount() == null) {
            supplier.setOverdueCount(0);
        }
        if (supplier.getTotalOverdueAmount() == null) {
            supplier.setTotalOverdueAmount(BigDecimal.ZERO);
        }
        updateSupplierLevel(supplier);
        storage.suppliers.put(supplier.getId(), supplier);
        return supplier;
    }

    public Supplier updateSupplier(String id, Supplier supplier) {
        Supplier existing = storage.suppliers.get(id);
        if (existing == null) {
            return null;
        }
        existing.setName(supplier.getName());
        existing.setContactPerson(supplier.getContactPerson());
        existing.setPhone(supplier.getPhone());
        existing.setEmail(supplier.getEmail());
        existing.setRemark(supplier.getRemark());
        existing.setUpdateTime(LocalDateTime.now());
        storage.suppliers.put(id, existing);
        return existing;
    }

    public Supplier getSupplier(String id) {
        return storage.suppliers.get(id);
    }

    public List<Supplier> getAllSuppliers() {
        return storage.suppliers.values().stream()
                .sorted(Comparator.comparing(Supplier::getScore).reversed())
                .collect(Collectors.toList());
    }

    public void updateSupplierScore(String contractId, int overdueDays, BigDecimal amount) {
        Contract contract = storage.contracts.get(contractId);
        if (contract == null) {
            return;
        }

        Supplier supplier = storage.suppliers.values().stream()
                .filter(s -> s.getName().equals(contract.getPartyB()))
                .findFirst()
                .orElse(null);

        if (supplier == null) {
            supplier = new Supplier();
            supplier.setName(contract.getPartyB());
            supplier.setScore(100);
            supplier.setOverdueCount(0);
            supplier.setTotalOverdueAmount(BigDecimal.ZERO);
            createSupplier(supplier);
        }

        int scoreDeduction = Math.min(overdueDays / 10 + 5, 30);
        supplier.setScore(Math.max(0, supplier.getScore() - scoreDeduction));
        supplier.setOverdueCount(supplier.getOverdueCount() + 1);
        supplier.setTotalOverdueAmount(supplier.getTotalOverdueAmount().add(amount));
        supplier.setUpdateTime(LocalDateTime.now());
        updateSupplierLevel(supplier);
        storage.suppliers.put(supplier.getId(), supplier);
    }

    private void updateSupplierLevel(Supplier supplier) {
        int score = supplier.getScore();
        if (score >= 90) {
            supplier.setLevel("A");
        } else if (score >= 75) {
            supplier.setLevel("B");
        } else if (score >= 60) {
            supplier.setLevel("C");
        } else {
            supplier.setLevel("D");
        }
    }
}
