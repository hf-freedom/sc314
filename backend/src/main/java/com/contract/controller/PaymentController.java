package com.contract.controller;

import com.contract.common.Result;
import com.contract.entity.PaymentNode;
import com.contract.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Result<PaymentNode> createPaymentNode(@RequestBody PaymentNode node) {
        return Result.success(paymentService.createPaymentNode(node));
    }

    @PutMapping("/{id}")
    public Result<PaymentNode> updatePaymentNode(@PathVariable String id, @RequestBody PaymentNode node) {
        PaymentNode updated = paymentService.updatePaymentNode(id, node);
        if (updated == null) {
            return Result.error("付款节点不存在");
        }
        return Result.success(updated);
    }

    @PostMapping("/{id}/pay")
    public Result<PaymentNode> markAsPaid(@PathVariable String id, @RequestBody(required = false) Map<String, String> params) {
        LocalDate actualDate = params != null && params.get("actualDate") != null
                ? LocalDate.parse(params.get("actualDate")) : null;
        PaymentNode updated = paymentService.markAsPaid(id, actualDate);
        if (updated == null) {
            return Result.error("付款节点不存在");
        }
        return Result.success(updated);
    }

    @GetMapping("/contract/{contractId}")
    public Result<List<PaymentNode>> getPaymentNodesByContract(@PathVariable String contractId) {
        return Result.success(paymentService.getPaymentNodesByContract(contractId));
    }

    @GetMapping("/overdue")
    public Result<List<PaymentNode>> getOverduePayments() {
        return Result.success(paymentService.getOverduePayments());
    }

    @GetMapping("/upcoming/{days}")
    public Result<List<PaymentNode>> getUpcomingPayments(@PathVariable int days) {
        return Result.success(paymentService.getUpcomingPayments(days));
    }

    @GetMapping
    public Result<List<PaymentNode>> getAllPaymentNodes() {
        return Result.success(paymentService.getAllPaymentNodes());
    }
}
