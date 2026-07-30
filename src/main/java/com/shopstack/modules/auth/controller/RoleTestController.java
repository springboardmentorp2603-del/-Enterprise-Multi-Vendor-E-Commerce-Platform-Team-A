package com.shopstack.modules.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleTestController {

    @GetMapping("/customer/test")
    public String customerAccess() {
        return "Customer Board - only CUSTOMER or ADMIN can see this";
    }

    @GetMapping("/vendor/test")
    public String vendorAccess() {
        return "Vendor Board - only VENDOR or ADMIN can see this";
    }

    @GetMapping("/warehouse/test")
    public String warehouseAccess() {
        return "Warehouse Board - only WAREHOUSE_STAFF or ADMIN can see this";
    }

    @GetMapping("/admin/test")
    public String adminAccess() {
        return "Admin Board - only ADMIN can see this";
    }
}