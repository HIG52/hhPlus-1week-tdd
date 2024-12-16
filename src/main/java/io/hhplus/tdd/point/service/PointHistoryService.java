package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PointHistoryService {

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    public List<PointHistory> listHistories(long id) {
        return pointHistoryRepository.listHistories(id);
    }
}
