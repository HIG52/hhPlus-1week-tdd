package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.UserPoint;
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

}
