package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.type.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointHistoryServiceImpl implements PointHistoryService {

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    public List<PointHistory> listHistories(long id) {
        return pointHistoryRepository.listHistories(id);
    }

    public PointHistory insertPointHistory(long id, long point, TransactionType transactionType) {
        return pointHistoryRepository.insertHistory(id, point, transactionType);
    }

}
