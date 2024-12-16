package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.type.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    public UserPoint viewPoint(long id) {
        return userPointRepository.selectById(id);
    }

    public UserPoint chargePoint(long id, long point) {
        long currentPoint = viewPoint(id).point();
        long chargePoint = point;
        long updatedPoint = currentPoint + chargePoint;

        insertPointHistory(id, point, TransactionType.CHARGE);

        return userPointRepository.updatePointById(id, updatedPoint);
    }


    public UserPoint usePoint(long id, long point) {
        long currentPoint = viewPoint(id).point();
        long usePoint = point;
        long updatedPoint = currentPoint - usePoint;

        if(updatedPoint < 0){
            throw new IllegalArgumentException("사용하려는 포인트가 잔고보다 많습니다.");
        }

        insertPointHistory(id, point, TransactionType.USE);

        return userPointRepository.updatePointById(id, updatedPoint);
    }

    private void insertPointHistory(long id, long point, TransactionType transactionType) {
        PointHistory pointHistory = pointHistoryRepository.insertHistory(id, point, transactionType);
    }
}
