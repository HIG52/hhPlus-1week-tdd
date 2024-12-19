package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.type.TransactionType;

import java.util.List;

public interface PointHistoryService {

    List<PointHistory> listHistories(long id);

    PointHistory insertPointHistory(long id, long point, TransactionType transactionType);
}
