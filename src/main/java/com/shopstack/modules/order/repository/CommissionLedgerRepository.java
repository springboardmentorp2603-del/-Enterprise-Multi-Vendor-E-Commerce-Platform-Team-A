package com.shopstack.modules.order.repository;

import com.shopstack.modules.order.entity.CommissionLedger;
import com.shopstack.modules.order.enums.LedgerTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, UUID> {

    List<CommissionLedger> findByOrderId(UUID orderId);

    List<CommissionLedger> findByOrderItemId(UUID orderItemId);

    boolean existsByOrderItemIdAndTransactionType(UUID orderItemId, LedgerTransactionType transactionType);

   @Query(value = """
    SELECT *
    FROM commission_ledger l
    WHERE l.vendor_id = :vendorId
      AND (CAST(:dateFrom AS timestamp) IS NULL OR l.created_at >= CAST(:dateFrom AS timestamp))
      AND (CAST(:dateTo AS timestamp) IS NULL OR l.created_at <= CAST(:dateTo AS timestamp))
      AND (CAST(:transactionType AS varchar) IS NULL OR l.transaction_type = CAST(:transactionType AS varchar))
      AND (CAST(:status AS varchar) IS NULL OR l.status = CAST(:status AS varchar))
      AND (CAST(:orderId AS uuid) IS NULL OR l.order_id = CAST(:orderId AS uuid))
      AND (CAST(:productId AS uuid) IS NULL OR l.product_id = CAST(:productId AS uuid))
    """,
    countQuery = """
    SELECT COUNT(*)
    FROM commission_ledger l
    WHERE l.vendor_id = :vendorId
      AND (CAST(:dateFrom AS timestamp) IS NULL OR l.created_at >= CAST(:dateFrom AS timestamp))
      AND (CAST(:dateTo AS timestamp) IS NULL OR l.created_at <= CAST(:dateTo AS timestamp))
      AND (CAST(:transactionType AS varchar) IS NULL OR l.transaction_type = CAST(:transactionType AS varchar))
      AND (CAST(:status AS varchar) IS NULL OR l.status = CAST(:status AS varchar))
      AND (CAST(:orderId AS uuid) IS NULL OR l.order_id = CAST(:orderId AS uuid))
      AND (CAST(:productId AS uuid) IS NULL OR l.product_id = CAST(:productId AS uuid))
    """,
    nativeQuery = true)
Page<CommissionLedger> findWithFilters(
        @Param("vendorId") Long vendorId,
        @Param("dateFrom") LocalDateTime dateFrom,
        @Param("dateTo") LocalDateTime dateTo,
        @Param("transactionType") LedgerTransactionType transactionType,
        @Param("status") String status,
        @Param("orderId") UUID orderId,
        @Param("productId") UUID productId,
        Pageable pageable);

    @Query("SELECT l FROM CommissionLedger l WHERE l.vendorId = :vendorId AND l.createdAt BETWEEN :start AND :end AND l.status = 'CONFIRMED'")
    List<CommissionLedger> findAllByVendorIdAndRange(
            @Param("vendorId") Long vendorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(l.grossAmount), 0) FROM CommissionLedger l WHERE l.vendorId = :vendorId " +
           "AND l.transactionType = :type AND l.status = 'CONFIRMED'")
    BigDecimal sumGrossAmountByVendorIdAndType(
            @Param("vendorId") Long vendorId,
            @Param("type") LedgerTransactionType type);

    @Query("SELECT COALESCE(SUM(l.commissionAmount), 0) FROM CommissionLedger l WHERE l.vendorId = :vendorId " +
           "AND l.transactionType = :type AND l.status = 'CONFIRMED'")
    BigDecimal sumCommissionAmountByVendorIdAndType(
            @Param("vendorId") Long vendorId,
            @Param("type") LedgerTransactionType type);

    @Query("SELECT COALESCE(SUM(l.vendorAmount), 0) FROM CommissionLedger l WHERE l.vendorId = :vendorId " +
           "AND l.status = 'CONFIRMED'")
    BigDecimal sumVendorAmountByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(DISTINCT l.orderId) FROM CommissionLedger l WHERE l.vendorId = :vendorId AND l.status = 'CONFIRMED'")
    long countOrdersByVendorId(@Param("vendorId") Long vendorId);
}
