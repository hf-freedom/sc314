package com.contract.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Contract {
    private String id;
    private String contractNo;
    private String name;
    private String partyA;
    private String partyB;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private BigDecimal amount;
    private String responsiblePerson;
    private String department;
    private String contractType;
    private String status;
    private String content;
    private LocalDate createTime;
    private LocalDate updateTime;
    private List<PaymentNode> paymentNodes = new ArrayList<>();
    private List<ContractVersion> versions = new ArrayList<>();
    private Integer currentVersion;
    private Boolean isHighAmount;
    private RenewalAssessment renewalAssessment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartyA() {
        return partyA;
    }

    public void setPartyA(String partyA) {
        this.partyA = partyA;
    }

    public String getPartyB() {
        return partyB;
    }

    public void setPartyB(String partyB) {
        this.partyB = partyB;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDate createTime) {
        this.createTime = createTime;
    }

    public LocalDate getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDate updateTime) {
        this.updateTime = updateTime;
    }

    public List<PaymentNode> getPaymentNodes() {
        return paymentNodes;
    }

    public void setPaymentNodes(List<PaymentNode> paymentNodes) {
        this.paymentNodes = paymentNodes;
    }

    public List<ContractVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ContractVersion> versions) {
        this.versions = versions;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Boolean getIsHighAmount() {
        return isHighAmount;
    }

    public void setIsHighAmount(Boolean isHighAmount) {
        this.isHighAmount = isHighAmount;
    }

    public RenewalAssessment getRenewalAssessment() {
        return renewalAssessment;
    }

    public void setRenewalAssessment(RenewalAssessment renewalAssessment) {
        this.renewalAssessment = renewalAssessment;
    }
}
