package com.contract.service;

import cn.hutool.core.util.IdUtil;
import com.contract.entity.Contract;
import com.contract.entity.PaymentNode;
import com.contract.entity.ReminderTask;
import com.contract.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    @Autowired
    private InMemoryStorage storage;

    @Value("${contract.warn-days.level1:90}")
    private int warnDaysLevel1;

    @Value("${contract.warn-days.level2:30}")
    private int warnDaysLevel2;

    @Value("${contract.warn-days.level3:7}")
    private int warnDaysLevel3;

    public ReminderTask createReminder(ReminderTask task) {
        task.setId(IdUtil.simpleUUID());
        task.setCreateTime(LocalDateTime.now());
        task.setStatus("PENDING");
        storage.reminderTasks.put(task.getId(), task);
        return task;
    }

    public ReminderTask completeReminder(String id, String remark) {
        ReminderTask task = storage.reminderTasks.get(id);
        if (task == null) {
            return null;
        }
        task.setStatus("COMPLETED");
        task.setCompletedTime(LocalDateTime.now());
        task.setRemark(remark);
        storage.reminderTasks.put(id, task);
        return task;
    }

    public List<ReminderTask> getRemindersByContract(String contractId) {
        return storage.reminderTasks.values().stream()
                .filter(r -> contractId.equals(r.getContractId()))
                .sorted(Comparator.comparing(ReminderTask::getRemindDate).reversed())
                .collect(Collectors.toList());
    }

    public List<ReminderTask> getPendingReminders() {
        return storage.reminderTasks.values().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .sorted(Comparator.comparing(ReminderTask::getRemindDate))
                .collect(Collectors.toList());
    }

    public List<ReminderTask> getAllReminders() {
        return new ArrayList<>(storage.reminderTasks.values());
    }

    public void generatePaymentReminders(PaymentNode node) {
        LocalDate payDate = node.getPaymentDate();
        String contractName = storage.contracts.get(node.getContractId()) != null
                ? storage.contracts.get(node.getContractId()).getName() : "未知合同";

        ReminderTask task = new ReminderTask();
        task.setContractId(node.getContractId());
        task.setContractName(contractName);
        task.setType("PAYMENT");
        task.setTitle("付款提醒：" + node.getNodeName());
        task.setContent("合同【" + contractName + "】的付款节点【" + node.getNodeName() + "】将于 " + payDate + " 到期，金额：" + node.getAmount() + " 元");
        task.setRemindDate(payDate.minusDays(7));
        task.setResponsiblePerson(storage.contracts.get(node.getContractId()) != null
                ? storage.contracts.get(node.getContractId()).getResponsiblePerson() : "");
        task.setPriority("MEDIUM");
        createReminder(task);
    }

    public void generateExpiryReminders(Contract contract, int days, int level) {
        String priority = level == 1 ? "LOW" : level == 2 ? "MEDIUM" : "HIGH";
        ReminderTask task = new ReminderTask();
        task.setContractId(contract.getId());
        task.setContractName(contract.getName());
        task.setType("EXPIRY");
        task.setTitle("合同到期提醒（" + level + "级）");
        task.setContent("合同【" + contract.getName() + "】将于 " + days + " 天后到期，到期日期：" + contract.getExpiryDate());
        task.setRemindDate(LocalDate.now());
        task.setResponsiblePerson(contract.getResponsiblePerson());
        task.setPriority(priority);
        task.setWarnLevel(level);
        createReminder(task);
    }

    public void generateOverdueReminder(PaymentNode node) {
        String contractName = storage.contracts.get(node.getContractId()) != null
                ? storage.contracts.get(node.getContractId()).getName() : "未知合同";
        ReminderTask task = new ReminderTask();
        task.setContractId(node.getContractId());
        task.setContractName(contractName);
        task.setType("OVERDUE");
        task.setTitle("付款逾期提醒");
        task.setContent("合同【" + contractName + "】的付款节点【" + node.getNodeName() + "】已逾期 " + node.getOverdueDays() + " 天，金额：" + node.getAmount() + " 元");
        task.setRemindDate(LocalDate.now());
        task.setResponsiblePerson(storage.contracts.get(node.getContractId()) != null
                ? storage.contracts.get(node.getContractId()).getResponsiblePerson() : "");
        task.setPriority("HIGH");
        createReminder(task);
    }
}
