package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final UserPointRepository userPointRepository;

    public PointService(UserPointRepository userPointRepository) {
        this.userPointRepository = userPointRepository;
    }

    public UserPoint viewPoint(long id) {
        return userPointRepository.selectById(id);
    }

    public UserPoint chargePoint(long id, long point) {
        long currentPoint = viewPoint(id).point();
        long chargePoint = point;
        long updatedPoint = currentPoint + chargePoint;

        return userPointRepository.updatePointById(id, updatedPoint);
    }
}
