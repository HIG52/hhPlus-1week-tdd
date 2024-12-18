package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.service.TestPointReset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PointConcurrencyIntegrationTest {

    // 테스트할 유저 ID
    public static final long USER_ID = 1L;
    // 동시에 실행할 스레드 수
    public static final int THREAD_COUNT = 20;

    @Autowired
    private PointService pointService;

    @Autowired
    private TestPointReset testPointReset;

    @BeforeEach
    void setUp() {
        testPointReset.setUserPointReset(USER_ID);
    }

    @Test
    public void 한_유저가_포인트_충전을_동시에_여러번_했을경우_순차적으로_처리해준다() throws InterruptedException {
        //given
        long chargeAmount = 100L; // 각 요청당 충전할 포인트

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        //when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(USER_ID, chargeAmount);
                } finally {
                    latch.countDown(); // 스레드 작업 완료
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        //then
        UserPoint finalPoint = pointService.viewPoint(USER_ID);
        assertThat(finalPoint.point()).isEqualTo(THREAD_COUNT * chargeAmount);
    }

    @Test
    public void 한_유저가_포인트_충전과_조회를_동시에_했을경우_순차적으로_처리해준다() throws InterruptedException {
        //given
        long chargePoint = 100L; // 각 요청당 충전할 포인트

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        List<Long> userPointList = Collections.synchronizedList(new ArrayList<Long>());

        //when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(USER_ID, chargePoint);
                    UserPoint viewPoint = pointService.viewPoint(USER_ID);
                    userPointList.add(viewPoint.point());
                } finally {
                    latch.countDown(); // 스레드 작업 완료
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        //then
        UserPoint finalPoint = pointService.viewPoint(USER_ID);
        assertThat(finalPoint.point()).isEqualTo(THREAD_COUNT * chargePoint);

        assertThat(userPointList.size()).isEqualTo(THREAD_COUNT);

        for(long point : userPointList) {
            assertThat(point).isEqualTo(THREAD_COUNT * chargePoint);
        }
    }

    @Test
    public void 한_유저가_포인트_충전과_사용를_동시에_했을경우_순차적으로_처리해준다() throws InterruptedException {
        //given

        long chargePoint = 200L; // 각 요청당 충전할 포인트
        long usePoint = 100L; // 각 요청당 사용할 포인트


        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        List<Long> userPointList = Collections.synchronizedList(new ArrayList<Long>());
        //when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(USER_ID, chargePoint);
                    pointService.usePoint(USER_ID, usePoint);
                    UserPoint viewPoint = pointService.viewPoint(USER_ID);
                    userPointList.add(viewPoint.point());
                } finally {
                    latch.countDown(); // 스레드 작업 완료
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        //then
        UserPoint finalPoint = pointService.viewPoint(USER_ID);
        assertThat(finalPoint.point()).isEqualTo((THREAD_COUNT * chargePoint) - (THREAD_COUNT * usePoint));

        assertThat(userPointList.size()).isEqualTo(THREAD_COUNT);

        for(long point : userPointList) {
            assertThat(point).isEqualTo(THREAD_COUNT * (chargePoint-usePoint));
        }
    }

    @Test
    public void 한_유저가_포인트_충전과_사용_조회를_동시에_했을경우_순차적으로_처리해준다() throws InterruptedException {
        //given

        long chargePoint = 200L; // 각 요청당 충전할 포인트
        long usePoint = 100L; // 각 요청당 사용할 포인트


        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        //when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(USER_ID, chargePoint);
                    pointService.usePoint(USER_ID, usePoint);
                } finally {
                    latch.countDown(); // 스레드 작업 완료
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        //then
        UserPoint finalPoint = pointService.viewPoint(USER_ID);
        assertThat(finalPoint.point()).isEqualTo((THREAD_COUNT * chargePoint) - (THREAD_COUNT * usePoint));
    }

    @Test
    public void 한_유저가_포인트_충전과_사용를_동시에_했을때_충전포인트보다_사용포인트가_많을경우_IllegalArgumentException을_반환한다() throws InterruptedException {
        //given
        long chargePoint = 200L; // 각 요청당 충전할 포인트
        long usePoint = 300L; // 각 요청당 사용할 포인트

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        List<Exception> exceptionList = Collections.synchronizedList(new ArrayList<Exception>());

        //when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(USER_ID, chargePoint);
                    pointService.usePoint(USER_ID, usePoint);
                } catch (IllegalArgumentException e){
                    exceptionList.add(e);
                } finally {
                    latch.countDown(); // 스레드 작업 완료
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();


        //then
        assertThat(exceptionList).isNotEmpty(); //예외가 무조건 발생해야한다.
        for(Exception e : exceptionList) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }


}
