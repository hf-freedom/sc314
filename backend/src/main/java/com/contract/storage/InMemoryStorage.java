package com.contract.storage;

import com.contract.entity.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryStorage {
    public final ConcurrentHashMap<String, Contract> contracts = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, PaymentNode> paymentNodes = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, ReminderTask> reminderTasks = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, ContractVersion> contractVersions = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, RenewalAssessment> renewalAssessments = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Supplier> suppliers = new ConcurrentHashMap<>();
}
