package com.perishable.domain.repository;

import com.perishable.domain.model.WastageRecord;
import java.time.LocalDate;
import java.util.List;

public interface WastageRepository {
    void save(WastageRecord record);
    List<WastageRecord> findBetween(LocalDate from, LocalDate to);
    List<WastageRecord> findByProductId(int productId);
}
