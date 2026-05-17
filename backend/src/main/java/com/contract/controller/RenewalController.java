package com.contract.controller;

import com.contract.common.Result;
import com.contract.entity.RenewalAssessment;
import com.contract.service.RenewalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/renewals")
@CrossOrigin
public class RenewalController {

    @Autowired
    private RenewalService renewalService;

    @PostMapping
    public Result<RenewalAssessment> createAssessment(@RequestBody RenewalAssessment assessment) {
        return Result.success(renewalService.createAssessment(assessment));
    }

    @PostMapping("/{id}/approve")
    public Result<RenewalAssessment> approveAssessment(@PathVariable String id, @RequestBody Map<String, String> params) {
        String approver = params.get("approver");
        String remark = params.get("remark");
        RenewalAssessment assessment = renewalService.approveAssessment(id, approver, remark);
        if (assessment == null) {
            return Result.error("评估不存在");
        }
        return Result.success(assessment);
    }

    @PostMapping("/{id}/reject")
    public Result<RenewalAssessment> rejectAssessment(@PathVariable String id, @RequestBody Map<String, String> params) {
        String approver = params.get("approver");
        String remark = params.get("remark");
        RenewalAssessment assessment = renewalService.rejectAssessment(id, approver, remark);
        if (assessment == null) {
            return Result.error("评估不存在");
        }
        return Result.success(assessment);
    }

    @GetMapping("/{id}")
    public Result<RenewalAssessment> getAssessment(@PathVariable String id) {
        RenewalAssessment assessment = renewalService.getAssessment(id);
        if (assessment == null) {
            return Result.error("评估不存在");
        }
        return Result.success(assessment);
    }

    @GetMapping("/contract/{contractId}")
    public Result<List<RenewalAssessment>> getAssessmentsByContract(@PathVariable String contractId) {
        return Result.success(renewalService.getAssessmentsByContract(contractId));
    }

    @GetMapping
    public Result<List<RenewalAssessment>> getAllAssessments() {
        return Result.success(renewalService.getAllAssessments());
    }
}
