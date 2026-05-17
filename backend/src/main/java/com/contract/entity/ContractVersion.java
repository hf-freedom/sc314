package com.contract.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ContractVersion {
    private String id;
    private String contractId;
    private Integer version;
    private String name;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private BigDecimal amount;
    private String responsiblePerson;
    private String content;
    private String changeReason;
    private String approver;
    private String approvalStatus;
    private LocalDateTime approvalTime;
    private LocalDateTime createTime;
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
