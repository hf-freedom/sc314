package com.contract.service;

import cn.hutool.core.util.IdUtil;
import com.contract.entity.*;
import com.contract.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractService {

    @Autowired
    private InMemoryStorage storage;

    @Value("${contract.high-amount-threshold:1000000}")
    private BigDecimal highAmountThreshold;

    public Contract createContract(Contract contract) {
        String id = IdUtil.simpleUUID();
        contract.setId(id);
        contract.setCreateTime(LocalDate.now());
        contract.setUpdateTime(LocalDate.now());
        contract.setStatus("ACTIVE");
        contract.setCurrentVersion(1);
        contract.setIsHighAmount(contract.getAmount().compareTo(highAmountThreshold) >= 0);

        ContractVersion version = new ContractVersion();
        version.setId(IdUtil.simpleUUID());
        version.setContractId(id);
        version.setVersion(1);
        version.setName(contract.getName());
        version.setEffectiveDate(contract.getEffectiveDate());
        version.setExpiryDate(contract.getExpiryDate());
        version.setAmount(contract.getAmount());
        version.setResponsiblePerson(contract.getResponsiblePerson());
        version.setContent(contract.getContent());
        version.setApprovalStatus("APPROVED");
        version.setApprovalTime(LocalDateTime.now());
        version.setCreateTime(LocalDateTime.now());
        storage.contractVersions.put(version.getId(), version);

        contract.getVersions().add(version);

        if (contract.getPaymentNodes() != null) {
            for (PaymentNode node : contract.getPaymentNodes()) {
                node.setId(IdUtil.simpleUUID());
                node.setContractId(id);
                node.setStatus("PENDING");
                node.setIsOverdue(false);
                node.setOverdueDays(0);
                storage.paymentNodes.put(node.getId(), node);
            }
        }

        storage.contracts.put(id, contract);
        return contract;
    }

    public Contract updateContract(String id, Contract contract, String changeReason, String approver) {
        Contract existing = storage.contracts.get(id);
        if (existing == null) {
            return null;
        }

        if ("CLOSED".equals(existing.getStatus())) {
            throw new RuntimeException("已关闭的合同不能修改");
        }

        int newVersion = existing.getCurrentVersion() + 1;

        ContractVersion pendingVersion = new ContractVersion();
        pendingVersion.setId(IdUtil.simpleUUID());
        pendingVersion.setContractId(id);
        pendingVersion.setVersion(newVersion);
        pendingVersion.setName(contract.getName());
        pendingVersion.setEffectiveDate(contract.getEffectiveDate());
        pendingVersion.setExpiryDate(contract.getExpiryDate());
        pendingVersion.setAmount(contract.getAmount());
        pendingVersion.setResponsiblePerson(contract.getResponsiblePerson());
        pendingVersion.setContent(contract.getContent());
        pendingVersion.setChangeReason(changeReason);
        pendingVersion.setApprover(approver);
        pendingVersion.setApprovalStatus("PENDING");
        pendingVersion.setCreateTime(LocalDateTime.now());
        storage.contractVersions.put(pendingVersion.getId(), pendingVersion);

        existing.getVersions().add(pendingVersion);
        existing.setUpdateTime(LocalDate.now());

        if (contract.getPaymentNodes() != null) {
            for (PaymentNode node : contract.getPaymentNodes()) {
                if (node.getId() == null) {
                    node.setId(IdUtil.simpleUUID());
                    node.setContractId(id);
                    node.setStatus("PENDING");
                    node.setIsOverdue(false);
                    node.setOverdueDays(0);
                    storage.paymentNodes.put(node.getId(), node);
                }
            }
        }

        storage.contracts.put(id, existing);
        return existing;
    }

    public ContractVersion approveVersion(String versionId, String approver, String contractId) {
        ContractVersion version = storage.contractVersions.get(versionId);
        if (version == null) {
            return null;
        }
        if (!"PENDING".equals(version.getApprovalStatus())) {
            throw new RuntimeException("该版本不是待审批状态");
        }

        version.setApprovalStatus("APPROVED");
        version.setApprover(approver);
        version.setApprovalTime(LocalDateTime.now());
        storage.contractVersions.put(versionId, version);

        Contract contract = storage.contracts.get(contractId);
        if (contract != null) {
            contract.setName(version.getName());
            contract.setEffectiveDate(version.getEffectiveDate());
            contract.setExpiryDate(version.getExpiryDate());
            contract.setAmount(version.getAmount());
            contract.setResponsiblePerson(version.getResponsiblePerson());
            contract.setContent(version.getContent());
            contract.setCurrentVersion(version.getVersion());
            contract.setIsHighAmount(version.getAmount().compareTo(highAmountThreshold) >= 0);
            contract.setUpdateTime(LocalDate.now());
            storage.contracts.put(contractId, contract);
        }

        return version;
    }

    public ContractVersion rejectVersion(String versionId, String approver, String remark) {
        ContractVersion version = storage.contractVersions.get(versionId);
        if (version == null) {
            return null;
        }
        if (!"PENDING".equals(version.getApprovalStatus())) {
            throw new RuntimeException("该版本不是待审批状态");
        }

        version.setApprovalStatus("REJECTED");
        version.setApprover(approver);
        version.setApprovalTime(LocalDateTime.now());
        version.setRemark(remark);
        storage.contractVersions.put(versionId, version);

        return version;
    }

    public boolean closeContract(String id) {
        Contract contract = storage.contracts.get(id);
        if (contract == null) {
            return false;
        }
        contract.setStatus("CLOSED");
        contract.setUpdateTime(LocalDate.now());
        storage.contracts.put(id, contract);

        storage.paymentNodes.values().stream()
                .filter(p -> id.equals(p.getContractId()))
                .forEach(p -> {
                    if (!"PAID".equals(p.getStatus())) {
                        p.setStatus("CANCELLED");
                        storage.paymentNodes.put(p.getId(), p);
                    }
                });

        storage.reminderTasks.values().stream()
                .filter(r -> id.equals(r.getContractId()) && !"COMPLETED".equals(r.getStatus()))
                .forEach(r -> {
                    r.setStatus("CANCELLED");
                    storage.reminderTasks.put(r.getId(), r);
                });

        return true;
    }

    public Contract getContract(String id) {
        return storage.contracts.get(id);
    }

    public List<Contract> getAllContracts() {
        return new ArrayList<>(storage.contracts.values());
    }

    public List<Contract> getExpiringContracts(int days) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(days);
        return storage.contracts.values().stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()))
                .filter(c -> !c.getExpiryDate().isAfter(threshold) && !c.getExpiryDate().isBefore(today))
                .sorted(Comparator.comparing(Contract::getExpiryDate))
                .collect(Collectors.toList());
    }

    public List<Contract> getHighAmountContracts() {
        return storage.contracts.values().stream()
                .filter(c -> "ACTIVE".equals(c.getStatus()))
                .filter(c -> Boolean.TRUE.equals(c.getIsHighAmount()))
                .collect(Collectors.toList());
    }
}
