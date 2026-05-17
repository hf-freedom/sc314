package com.contract.task;

import com.contract.entity.Contract;
import com.contract.entity.PaymentNode;
import com.contract.entity.ReminderTask;
import com.contract.service.*;
import com.contract.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ContractScheduledTask {

    @Autowired
    private InMemoryStorage storage;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RenewalService renewalService;

    @Value("${contract.warn-days.level1:90}")
    private int warnDaysLevel1;

    @Value("${contract.warn-days.level2:30}")
    private int warnDaysLevel2;

    @Value("${contract.warn-days.level3:7}")
    private int warnDaysLevel3;

    @Scheduled(cron = "${contract.cron.scan:0 0 8 * * ?}")
    public void scanContracts() {
        scanExpiringContracts();
        scanPayments();
        scanRenewals();
        scanAnomalies();
    }

    public void scanExpiringContracts() {
        LocalDate today = LocalDate.now();
        for (Contract contract : storage.contracts.values()) {
            if (!"ACTIVE".equals(contract.getStatus())) {
                continue;
            }
            long daysToExpiry = ChronoUnit.DAYS.between(today, contract.getExpiryDate());

            boolean existsLevel1 = hasExistingReminder(contract.getId(), "EXPIRY", 1);
            boolean existsLevel2 = hasExistingReminder(contract.getId(), "EXPIRY", 2);
            boolean existsLevel3 = hasExistingReminder(contract.getId(), "EXPIRY", 3);

            if (Boolean.TRUE.equals(contract.getIsHighAmount())) {
                if (daysToExpiry <= warnDaysLevel1 && daysToExpiry > warnDaysLevel2 && !existsLevel1) {
                    reminderService.generateExpiryReminders(contract, (int) daysToExpiry, 1);
                }
                if (daysToExpiry <= warnDaysLevel2 && daysToExpiry > warnDaysLevel3 && !existsLevel2) {
                    reminderService.generateExpiryReminders(contract, (int) daysToExpiry, 2);
                }
                if (daysToExpiry <= warnDaysLevel3 && daysToExpiry >= 0 && !existsLevel3) {
                    reminderService.generateExpiryReminders(contract, (int) daysToExpiry, 3);
                }
            } else {
                if (daysToExpiry <= warnDaysLevel2 && daysToExpiry > warnDaysLevel3 && !existsLevel2) {
                    reminderService.generateExpiryReminders(contract, (int) daysToExpiry, 2);
                }
                if (daysToExpiry <= warnDaysLevel3 && daysToExpiry >= 0 && !existsLevel3) {
                    reminderService.generateExpiryReminders(contract, (int) daysToExpiry, 3);
                }
            }
        }
    }

    public void scanPayments() {
        LocalDate today = LocalDate.now();
        for (PaymentNode node : storage.paymentNodes.values()) {
            if (!"PENDING".equals(node.getStatus())) {
                continue;
            }
            long daysToPay = ChronoUnit.DAYS.between(today, node.getPaymentDate());

            if (daysToPay == 7 && !hasPaymentReminder(node.getId())) {
                reminderService.generatePaymentReminders(node);
            }

            if (daysToPay < 0) {
                int overdueDays = (int) Math.abs(daysToPay);
                node.setIsOverdue(true);
                node.setOverdueDays(overdueDays);
                storage.paymentNodes.put(node.getId(), node);

                if (overdueDays % 7 == 0) {
                    reminderService.generateOverdueReminder(node);
                }
            }
        }
    }

    public void scanRenewals() {
        LocalDate today = LocalDate.now();
        for (Contract contract : storage.contracts.values()) {
            if (!"ACTIVE".equals(contract.getStatus())) {
                continue;
            }
            long daysToExpiry = ChronoUnit.DAYS.between(today, contract.getExpiryDate());
            if (daysToExpiry <= 60 && daysToExpiry >= 0) {
                renewalService.autoGenerateAssessment(contract);
            }
        }
    }

    public void scanAnomalies() {
        LocalDate today = LocalDate.now();
        for (Contract contract : storage.contracts.values()) {
            if (!"ACTIVE".equals(contract.getStatus())) {
                continue;
            }
            if (contract.getExpiryDate().isBefore(today)) {
                contract.setStatus("EXPIRED");
                storage.contracts.put(contract.getId(), contract);

                storage.paymentNodes.values().stream()
                        .filter(p -> contract.getId().equals(p.getContractId()) && !"PAID".equals(p.getStatus()))
                        .forEach(p -> {
                            p.setStatus("CANCELLED");
                            storage.paymentNodes.put(p.getId(), p);
                        });

                storage.reminderTasks.values().stream()
                        .filter(r -> contract.getId().equals(r.getContractId()) && "PENDING".equals(r.getStatus()))
                        .forEach(r -> {
                            r.setStatus("CANCELLED");
                            storage.reminderTasks.put(r.getId(), r);
                        });
            }
        }
    }

    private boolean hasExistingReminder(String contractId, String type, int level) {
        return storage.reminderTasks.values().stream()
                .anyMatch(r -> contractId.equals(r.getContractId())
                        && type.equals(r.getType())
                        && level == (r.getWarnLevel() != null ? r.getWarnLevel() : 0));
    }

    private boolean hasPaymentReminder(String paymentNodeId) {
        return storage.reminderTasks.values().stream()
                .anyMatch(r -> "PAYMENT".equals(r.getType())
                        && r.getContent() != null
                        && r.getContent().contains(paymentNodeId));
    }
}
