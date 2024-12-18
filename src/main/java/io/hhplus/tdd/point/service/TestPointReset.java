package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.repository.UserPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;


@Service
public class TestPointReset {

    @Autowired
    private UserPointRepository userPointRepository;

    public void setUserPointReset(long id){
        userPointRepository.updatePointById(id, 0); // 포인트를 0으로 설정
    }

}
