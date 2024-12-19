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
    public static final int THREAD_COUNT = 10;

    @Autowired
    private PointService pointService;

    @Autowired
    private TestPointReset testPointReset;

    @BeforeEach
    void setUp() {
        testPointReset.setUserPointReset(USER_ID);
    }

    @Test
    public void 유저가_포인트_충전을_동시에_여러번_했을경우_누락되지않고_처리된다() throws InterruptedException {
        //given
        long chargePoint = 100L; // 각 요청당 충전할 포인트

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        List<Runnable> tasks = List.of(
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint),
                () -> pointService.chargePoint(USER_ID, chargePoint)
        );

        //when
        tasks.forEach(task -> executorService.submit(() -> {
            try {
                task.run();
            } finally {
                latch.countDown();
            }
        }));

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        //then
        UserPoint finalPoint = pointService.viewPoint(USER_ID);
        assertThat(finalPoint.point()).isEqualTo(THREAD_COUNT * chargePoint);
    }

    @Test
    public void 유저가_포인트_사용을_동시에_여러번_했을경우_충전된포인트가_사용포인트보다_많다면_누락되지않고_처리된다() throws InterruptedException {
        //given
        long usePoint = 100L; // 각 요청당 사용할 포인트

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        pointService.chargePoint(USER_ID, 2000L);

        List<Runnable> tasks = List.of(
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint),
                () -> pointService.usePoint(USER_ID, usePoint)
        );

        //when
        tasks.forEach(task -> executorService.submit(() -> {
            try {
                task.run();
            } finally {
                latch.countDown();
            }
        }));

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        //then
        UserPoint finalPoint = pointService.viewPoint(USER_ID);
        assertThat(finalPoint.point()).isEqualTo(1000L);
    }

    @Test
    public void 유저가_포인트_사용을_동시에_여러번_했을경우_충전된포인트가_사용포인트보다_적다면_직전까지의_포인트사용을_처리후_IllegalArgumentException을_반환한다() throws InterruptedException {
        // given
        long usePoint = 100L; // 각 요청당 사용할 포인트
        long initialPoint = 500L; // 초기 포인트

        // 초기 포인트 충전
        pointService.chargePoint(USER_ID, initialPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>()); // 예외 기록
        List<String> successes = Collections.synchronizedList(new ArrayList<>()); // 성공 기록

        // 10개의 포인트 사용 요청
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    long updatedPoint = pointService.usePoint(USER_ID, usePoint).point();
                    successes.add("Success: " + updatedPoint);
                } catch (IllegalArgumentException e) {
                    exceptions.add(e); // 예외 기록
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 끝날 때까지 대기
        executorService.shutdown();

        // then
        // 성공적으로 사용된 요청 수 검증 (최대 5번만 성공해야 함)
        assertThat(successes).hasSize(5);

        // 예외가 발생한 요청 수 검증
        assertThat(exceptions).hasSize(THREAD_COUNT - 5);

        // 최종 포인트 검증 (0이 되어야 함)
        UserPoint finalPoint = pointService.viewPoint(USER_ID);
        assertThat(finalPoint.point()).isEqualTo(0L);

        // 예외 메시지 확인
        exceptions.forEach(e -> assertThat(e.getMessage()).contains("사용하려는 포인트가 잔고보다 많습니다."));
    }

    @Test
    public void 유저가_포인트_사용과_충전을_동시에_했을경우_가지고_있는_포인트가_사용포인트보다_많다면_기존포인트에_충전_및_사용_포인트를_계산하여_반환한다() throws InterruptedException {
        // given
        long chargePoint = 100L; // 각 요청당 충전할 포인트
        long usePoint = 200L; // 각 요청당 사용할 포인트
        long initialPoint = 5000L; // 초기 포인트

        // 초기 포인트 충전
        pointService.chargePoint(USER_ID, initialPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // 10개의 포인트 사용 요청
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(USER_ID, chargePoint);
                    pointService.usePoint(USER_ID, usePoint);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 끝날 때까지 대기
        executorService.shutdown();

        // then
        // 최종 포인트 검증 (0이 되어야 함)
        UserPoint finalPoint = pointService.viewPoint(USER_ID);
        assertThat(finalPoint.point()).isEqualTo(4000L);

    }
    
    @Test
    public void 유저가_포인트_사용과_충전을_동시에_했을경우_가지고_있는_포인트가_사용포인트보다_적다면_직전까지의_포인트사용을_처리후_IllegalArgumentException을_반환한다() throws InterruptedException {
        // given
        long chargePoint = 100L; // 각 요청당 충전할 포인트
        long usePoint = 200L; // 각 요청당 사용할 포인트
        long initialPoint = 500L; // 초기 포인트

        // 초기 포인트 충전
        pointService.chargePoint(USER_ID, initialPoint);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        List<String> successes = Collections.synchronizedList(new ArrayList<>()); // 성공 기록
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>()); // 예외 기록

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    // 충전과 사용을 동시에 실행
                    pointService.chargePoint(USER_ID, chargePoint);
                    pointService.usePoint(USER_ID, usePoint);
                    successes.add("Success");
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 끝날 때까지 대기
        executorService.shutdown();

        // then
        // 성공적으로 사용된 요청의 수와 예외 발생 수 검증
        int successfulTransactions = successes.size();
        int failedTransactions = exceptions.size();

        // 예상 값: 실패는 THREAD_COUNT 중 일부 스레드에서 발생
        System.out.println("Success count: " + successfulTransactions);
        System.out.println("Exception count: " + failedTransactions);

        // 최종 포인트 검증
        UserPoint finalPoint = pointService.viewPoint(USER_ID);

        // 예상 포인트 계산
        long totalCharge = THREAD_COUNT * chargePoint; // 총 충전된 포인트
        long totalUse = successfulTransactions * usePoint; // 총 사용된 포인트
        long expectedFinalPoint = initialPoint + totalCharge - totalUse;

        // 검증
        assertThat(finalPoint.point()).isEqualTo(expectedFinalPoint);

        // 예외 메시지 검증
        exceptions.forEach(e -> assertThat(e)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용하려는 포인트가 잔고보다 많습니다."));

    }






}
