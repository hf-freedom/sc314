package com.contract.service;

import cn.hutool.core.util.IdUtil;
import com.contract.entity.PaymentNode;
import com.contract.entity.Supplier;
import com.contract.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private InMemoryStorage storage;

    @Autowired
    private SupplierService supplierService;

    public PaymentNode createPaymentNode(PaymentNode node) {
        node.setId(IdUtil.simpleUUID());
        node.setStatus("PENDING");
        node.setIsOverdue(false);
        node.setOverdueDays(0);
        storage.paymentNodes.put(node.getId(), node);
        return node;
    }

    public PaymentNode updatePaymentNode(String id, PaymentNode node) {
        PaymentNode existing = storage.paymentNodes.get(id);
        if (existing == null) {
            return null;
        }
        existing.setNodeName(node.getNodeName());
        existing.setPaymentDate(node.getPaymentDate());
        existing.setAmount(node.getAmount());
        existing.setRemark(node.getRemark());
        storage.paymentNodes.put(id, existing);
        return existing;
    }

    public PaymentNode markAsPaid(String id, LocalDate actualDate) {
        PaymentNode node = storage.paymentNodes.get(id);
        if (node == null) {
            return null;
        }
        node.setStatus("PAID");
        node.setActualPaymentDate(actualDate != null ? actualDate : LocalDate.now());

        if (node.getActualPaymentDate().isAfter(node.getPaymentDate())) {
            int overdueDays = (int) ChronoUnit.DAYS.between(node.getPaymentDate(), node.getActualPaymentDate());
            node.setIsOverdue(true);
            node.setOverdueDays(overdueDays);
            supplierService.updateSupplierScore(node.getContractId(), overdueDays, node.getAmount());
        }

        storage.paymentNodes.put(id, node);
        return node;
    }

    public List<PaymentNode> getPaymentNodesByContract(String contractId) {
        return storage.paymentNodes.values().stream()
                .filter(p -> contractId.equals(p.getContractId()))
                .collect(Collectors.toList());
    }

    public List<PaymentNode> getOverduePayments() {
        LocalDate today = LocalDate.now();
        return storage.paymentNodes.values().stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .filter(p -> p.getPaymentDate().isBefore(today))
                .peek(p -> {
                    int overdueDays = (int) ChronoUnit.DAYS.between(p.getPaymentDate(), today);
                    p.setIsOverdue(true);
                    p.setOverdueDays(overdueDays);
                })
                .collect(Collectors.toList());
    }

    public List<PaymentNode> getUpcomingPayments(int days) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(days);
        return storage.paymentNodes.values().stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .filter(p -> !p.getPaymentDate().isBefore(today) && !p.getPaymentDate().isAfter(threshold))
                .collect(Collectors.toList());
    }

    public List<PaymentNode> getAllPaymentNodes() {
        return new ArrayList<>(storage.paymentNodes.values());
    }
}
