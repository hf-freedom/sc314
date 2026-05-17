package com.contract.service;

import cn.hutool.core.util.IdUtil;
import com.contract.entity.Contract;
import com.contract.entity.RenewalAssessment;
import com.contract.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RenewalService {

    @Autowired
    private InMemoryStorage storage;

    public RenewalAssessment createAssessment(RenewalAssessment assessment) {
        assessment.setId(IdUtil.simpleUUID());
        assessment.setCreateTime(LocalDateTime.now());
        assessment.setApprovalStatus("PENDING");
        storage.renewalAssessments.put(assessment.getId(), assessment);

        Contract contract = storage.contracts.get(assessment.getContractId());
        if (contract != null) {
            contract.setRenewalAssessment(assessment);
            storage.contracts.put(contract.getId(), contract);
        }
        return assessment;
    }

    public RenewalAssessment approveAssessment(String id, String approver, String remark) {
        RenewalAssessment assessment = storage.renewalAssessments.get(id);
        if (assessment == null) {
            return null;
        }
        assessment.setApprovalStatus("APPROVED");
        assessment.setApprover(approver);
        assessment.setApprovalTime(LocalDateTime.now());
        assessment.setRemark(remark);
        storage.renewalAssessments.put(id, assessment);
        return assessment;
    }

    public RenewalAssessment rejectAssessment(String id, String approver, String remark) {
        RenewalAssessment assessment = storage.renewalAssessments.get(id);
        if (assessment == null) {
            return null;
        }
        assessment.setApprovalStatus("REJECTED");
        assessment.setApprover(approver);
        assessment.setApprovalTime(LocalDateTime.now());
        assessment.setRemark(remark);
        storage.renewalAssessments.put(id, assessment);
        return assessment;
    }

    public RenewalAssessment getAssessment(String id) {
        return storage.renewalAssessments.get(id);
    }

    public List<RenewalAssessment> getAssessmentsByContract(String contractId) {
        return storage.renewalAssessments.values().stream()
                .filter(r -> contractId.equals(r.getContractId()))
                .sorted(Comparator.comparing(RenewalAssessment::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    public List<RenewalAssessment> getAllAssessments() {
        return new ArrayList<>(storage.renewalAssessments.values());
    }

    public RenewalAssessment autoGenerateAssessment(Contract contract) {
        RenewalAssessment existing = storage.renewalAssessments.values().stream()
                .filter(r -> contract.getId().equals(r.getContractId()) && "PENDING".equals(r.getApprovalStatus()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            return existing;
        }

        RenewalAssessment assessment = new RenewalAssessment();
        assessment.setContractId(contract.getId());
        assessment.setContractName(contract.getName());
        assessment.setExpiryDate(contract.getExpiryDate());
        assessment.setCurrentAmount(contract.getAmount());
        assessment.setPerformanceStatus("待评估");
        assessment.setRiskAssessment("待评估");
        assessment.setRenewalSuggestion("建议评估后决定");
        assessment.setExpectedAmount(contract.getAmount().toString());
        return createAssessment(assessment);
    }
}
