package com.shopstack.modules.vendor.config;

import com.shopstack.common.enums.AddressType;
import com.shopstack.common.enums.DocumentType;
import com.shopstack.common.enums.VendorStatus;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.UserRepository;
import com.shopstack.modules.vendor.entity.*;
import com.shopstack.modules.vendor.repository.VendorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(4)
public class VendorSeeder implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(VendorSeeder.class);

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    public VendorSeeder(VendorRepository vendorRepository,
                        UserRepository userRepository) {

        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {

        if (vendorRepository.count() > 0) {
            return;
        }

        User admin = userRepository.findByEmail("admin@shopstack.com")
                .orElse(null);

        if (admin == null) {

            log.warn("Admin user not found. Vendor seeding skipped.");
            return;
        }

        Vendor vendor = Vendor.builder()
                .user(admin)
                .businessName("ShopStack Electronics")
                .businessEmail("vendor@shopstack.com")
                .businessPhone("9876543210")
                .businessType("Electronics")
                .gstNumber("22ABCDE1234F1Z5")
                .panNumber("ABCDE1234F")
                .description("Official ShopStack Vendor")
                .logoUrl("https://picsum.photos/300")
                .status(VendorStatus.APPROVED)
                .build();

        VendorAddress address = VendorAddress.builder()
                .vendor(vendor)
                .addressType(AddressType.BUSINESS)
                .addressLine1("Madhapur")
                .city("Hyderabad")
                .state("Telangana")
                .country("India")
                .postalCode("500081")
                .isDefault(true)
                .build();

        VendorBankDetails bank = VendorBankDetails.builder()
                .vendor(vendor)
                .accountHolderName("ShopStack Electronics")
                .accountNumber("123456789012")
                .bankName("State Bank of India")
                .ifscOrSwiftCode("SBIN0001234")
                .build();

        VendorDocument document = VendorDocument.builder()
                .vendor(vendor)
                .documentType(DocumentType.GST_CERTIFICATE)
                .documentUrl("https://example.com/gst.pdf")
                .build();

        vendor.setAddresses(List.of(address));
        vendor.setBankDetails(List.of(bank));
        vendor.setDocuments(List.of(document));

        vendorRepository.save(vendor);

        log.info("Sample Vendor Seeded Successfully");
    }
}