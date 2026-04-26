package com.example.inventory.repository;

import com.example.inventory.entity.InventoryTransaction;
import com.example.inventory.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @Query("""
            select date(t.timestamp), sum(t.quantity)
            from InventoryTransaction t
            where t.type = :type
            group by date(t.timestamp)
            order by date(t.timestamp)
            """)
    List<Object[]> getDailyTotals(TransactionType type);
}
