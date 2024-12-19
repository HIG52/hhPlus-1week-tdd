package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.type.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointHistoryRepository {

    @Autowired
    private PointHistoryTable pointHistoryTable;


    public PointHistory insertHistory(long id, long point, TransactionType transactionType) {
        return pointHistoryTable.insert(id, point, transactionType, System.currentTimeMillis());
    }

    public List<PointHistory> listHistories(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }
}
