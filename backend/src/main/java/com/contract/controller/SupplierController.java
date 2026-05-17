package com.contract.controller;

import com.contract.common.Result;
import com.contract.entity.Supplier;
import com.contract.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @PostMapping
    public Result<Supplier> createSupplier(@RequestBody Supplier supplier) {
        return Result.success(supplierService.createSupplier(supplier));
    }

    @PutMapping("/{id}")
    public Result<Supplier> updateSupplier(@PathVariable String id, @RequestBody Supplier supplier) {
        Supplier updated = supplierService.updateSupplier(id, supplier);
        if (updated == null) {
            return Result.error("供应商不存在");
        }
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    public Result<Supplier> getSupplier(@PathVariable String id) {
        Supplier supplier = supplierService.getSupplier(id);
        if (supplier == null) {
            return Result.error("供应商不存在");
        }
        return Result.success(supplier);
    }

    @GetMapping
    public Result<List<Supplier>> getAllSuppliers() {
        return Result.success(supplierService.getAllSuppliers());
    }
}
