package com.contract.controller;

import com.contract.common.Result;
import com.contract.entity.Contract;
import com.contract.entity.PaymentNode;
import com.contract.entity.ReminderTask;
import com.contract.entity.Supplier;
import com.contract.service.*;
import com.contract.storage.InMemoryStorage;
import com.contract.task.ContractScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@CrossOrigin
public class SystemController {

    @Autowired
    private ContractScheduledTask scheduledTask;

    @Autowired
    private InMemoryStorage storage;

    @Autowired
    private ContractService contractService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private RenewalService renewalService;

    @PostMapping("/scan")
    public Result<Map<String, Object>> scanAll() {
        scheduledTask.scanContracts();
        return getStatistics();
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Contract> contracts = contractService.getAllContracts();
        List<PaymentNode> payments = paymentService.getAllPaymentNodes();
        List<ReminderTask> reminders = reminderService.getAllReminders();
        List<Supplier> suppliers = supplierService.getAllSuppliers();

        stats.put("totalContracts", contracts.size());
        stats.put("activeContracts", contracts.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count());
        stats.put("expiredContracts", contracts.stream().filter(c -> "EXPIRED".equals(c.getStatus())).count());
        stats.put("closedContracts", contracts.stream().filter(c -> "CLOSED".equals(c.getStatus())).count());
        stats.put("highAmountContracts", contracts.stream().filter(c -> Boolean.TRUE.equals(c.getIsHighAmount())).count());

        stats.put("totalPayments", payments.size());
        stats.put("pendingPayments", payments.stream().filter(p -> "PENDING".equals(p.getStatus())).count());
        stats.put("paidPayments", payments.stream().filter(p -> "PAID".equals(p.getStatus())).count());
        stats.put("overduePayments", payments.stream().filter(p -> Boolean.TRUE.equals(p.getIsOverdue())).count());

        stats.put("totalReminders", reminders.size());
        stats.put("pendingReminders", reminders.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        stats.put("completedReminders", reminders.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count());

        stats.put("totalSuppliers", suppliers.size());
        stats.put("levelASuppliers", suppliers.stream().filter(s -> "A".equals(s.getLevel())).count());
        stats.put("levelBSuppliers", suppliers.stream().filter(s -> "B".equals(s.getLevel())).count());
        stats.put("levelCSuppliers", suppliers.stream().filter(s -> "C".equals(s.getLevel())).count());
        stats.put("levelDSuppliers", suppliers.stream().filter(s -> "D".equals(s.getLevel())).count());

        stats.put("totalRenewals", renewalService.getAllAssessments().size());
        stats.put("pendingRenewals", renewalService.getAllAssessments().stream().filter(r -> "PENDING".equals(r.getApprovalStatus())).count());

        return Result.success(stats);
    }
}
