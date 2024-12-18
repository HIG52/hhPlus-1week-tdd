package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.type.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    //각 ID에 대한 락관리
    private final ConcurrentHashMap<Long, Lock> lockMap = new ConcurrentHashMap<>();

    private Lock getLock(long id) {
        return lockMap.computeIfAbsent(id, key -> new ReentrantLock(true)); //공정성 선입선출
    }

    public UserPoint viewPoint(long id) {
        Lock lock = getLock(id);
        lock.lock(); //락 획득
        try{
            return userPointRepository.selectById(id);
        }finally {
            lock.unlock(); //락 해제
        }

    }

    public UserPoint chargePoint(long id, long point) {
        //id별로 락 생성
        Lock lock = getLock(id);
        lock.lock(); //락 획득

        try {
            long currentPoint = viewPoint(id).point();
            long chargePoint = point;
            long updatedPoint = currentPoint + chargePoint;

            chargePointException(chargePoint);

            insertPointHistory(id, point, TransactionType.CHARGE);

            return userPointRepository.updatePointById(id, updatedPoint);

        }finally {
            lock.unlock(); //락 해제
        }
    }


    public UserPoint usePoint(long id, long point) {
        Lock lock = getLock(id);
        lock.lock(); //락 획득
        try{
            long currentPoint = viewPoint(id).point();
            long usePoint = point;
            long updatedPoint = currentPoint - usePoint;

            if(usePoint < 100 || usePoint > 10000) {
                throw  new IllegalArgumentException("포인트 사용 최소값은 100 이상 10,000이하여야 합니다.");
            }

            if(updatedPoint < 0){
                throw new IllegalArgumentException("사용하려는 포인트가 잔고보다 많습니다.");
            }

            insertPointHistory(id, point, TransactionType.USE);

            return userPointRepository.updatePointById(id, updatedPoint);

        }finally {
            lock.unlock(); //락 해제
        }

    }

    private static void chargePointException(long chargePoint) {
        if (chargePoint > 10000){
            throw new IllegalArgumentException("충전 포인트는 10,000이하여야 합니다.");
        }

        if(chargePoint < 100){
            throw new IllegalArgumentException("충전 포인트는 100이상이여야 합니다.");
        }
    }

    private void insertPointHistory(long id, long point, TransactionType transactionType) {
        PointHistory pointHistory = pointHistoryRepository.insertHistory(id, point, transactionType);
    }
}
