package com.contract.controller;

import com.contract.common.Result;
import com.contract.entity.ReminderTask;
import com.contract.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @PostMapping
    public Result<ReminderTask> createReminder(@RequestBody ReminderTask task) {
        return Result.success(reminderService.createReminder(task));
    }

    @PostMapping("/{id}/complete")
    public Result<ReminderTask> completeReminder(@PathVariable String id, @RequestBody(required = false) Map<String, String> params) {
        String remark = params != null ? params.get("remark") : null;
        ReminderTask task = reminderService.completeReminder(id, remark);
        if (task == null) {
            return Result.error("提醒任务不存在");
        }
        return Result.success(task);
    }

    @GetMapping("/contract/{contractId}")
    public Result<List<ReminderTask>> getRemindersByContract(@PathVariable String contractId) {
        return Result.success(reminderService.getRemindersByContract(contractId));
    }

    @GetMapping("/pending")
    public Result<List<ReminderTask>> getPendingReminders() {
        return Result.success(reminderService.getPendingReminders());
    }

    @GetMapping
    public Result<List<ReminderTask>> getAllReminders() {
        return Result.success(reminderService.getAllReminders());
    }
}
