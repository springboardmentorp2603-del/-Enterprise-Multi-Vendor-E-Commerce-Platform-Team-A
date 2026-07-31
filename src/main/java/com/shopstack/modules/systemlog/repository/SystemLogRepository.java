package com.shopstack.modules.systemlog.repository;

import com.shopstack.modules.systemlog.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, UUID> {

    List<SystemLog> findAllByOrderByCreatedAtDesc();

    @Query("SELECT s FROM SystemLog s WHERE s.createdAt BETWEEN :from AND :to " +
            "AND (:module IS NULL OR s.module = :module) ORDER BY s.createdAt DESC")
    List<SystemLog> search(@Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to,
                            @Param("module") String module);
}