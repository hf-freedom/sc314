package com.contract.controller;

import com.contract.common.Result;
import com.contract.entity.Contract;
import com.contract.entity.ContractVersion;
import com.contract.service.ContractService;
import com.contract.task.ContractScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractScheduledTask scheduledTask;

    @PostMapping
    public Result<Contract> createContract(@RequestBody Contract contract) {
        Contract created = contractService.createContract(contract);
        scheduledTask.scanContracts();
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Contract> updateContract(@PathVariable String id, @RequestBody Map<String, Object> params) {
        Contract contract = new Contract();
        contract.setName((String) params.get("name"));
        contract.setPartyA((String) params.get("partyA"));
        contract.setPartyB((String) params.get("partyB"));
        contract.setEffectiveDate(java.time.LocalDate.parse((String) params.get("effectiveDate")));
        contract.setExpiryDate(java.time.LocalDate.parse((String) params.get("expiryDate")));
        contract.setAmount(new java.math.BigDecimal(params.get("amount").toString()));
        contract.setResponsiblePerson((String) params.get("responsiblePerson"));
        contract.setDepartment((String) params.get("department"));
        contract.setContractType((String) params.get("contractType"));
        contract.setContent((String) params.get("content"));
        String changeReason = (String) params.get("changeReason");
        String approver = (String) params.get("approver");

        Contract updated = contractService.updateContract(id, contract, changeReason, approver);
        if (updated == null) {
            return Result.error("合同不存在");
        }
        scheduledTask.scanContracts();
        return Result.success(updated);
    }

    @PostMapping("/{id}/close")
    public Result<Void> closeContract(@PathVariable String id) {
        boolean closed = contractService.closeContract(id);
        if (!closed) {
            return Result.error("合同不存在");
        }
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Contract> getContract(@PathVariable String id) {
        Contract contract = contractService.getContract(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        return Result.success(contract);
    }

    @GetMapping
    public Result<List<Contract>> getAllContracts() {
        return Result.success(contractService.getAllContracts());
    }

    @GetMapping("/expiring/{days}")
    public Result<List<Contract>> getExpiringContracts(@PathVariable int days) {
        return Result.success(contractService.getExpiringContracts(days));
    }

    @GetMapping("/high-amount")
    public Result<List<Contract>> getHighAmountContracts() {
        return Result.success(contractService.getHighAmountContracts());
    }

    @PostMapping("/versions/{versionId}/approve")
    public Result<ContractVersion> approveVersion(@PathVariable String versionId, @RequestBody Map<String, String> params) {
        String approver = params.get("approver");
        String contractId = params.get("contractId");
        ContractVersion version = contractService.approveVersion(versionId, approver, contractId);
        if (version == null) {
            return Result.error("版本不存在");
        }
        scheduledTask.scanContracts();
        return Result.success(version);
    }

    @PostMapping("/versions/{versionId}/reject")
    public Result<ContractVersion> rejectVersion(@PathVariable String versionId, @RequestBody Map<String, String> params) {
        String approver = params.get("approver");
        String remark = params.get("remark");
        ContractVersion version = contractService.rejectVersion(versionId, approver, remark);
        if (version == null) {
            return Result.error("版本不存在");
        }
        return Result.success(version);
    }
}
