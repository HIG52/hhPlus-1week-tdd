package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    PointService pointService;

    @Mock
    UserPointRepository userPointRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @Test
    public void 유저의_id를_입력받으면_userPoint를_반환(){  //유저의 포인트가 정상적으로 반환이 되는지 확인
        //given
        long id = 1L;
        long point = 1000L;
        UserPoint userPoint = UserPoint.builder()
                .id(id)
                .point(point)
                .build();

        given(userPointRepository.selectById(id)).willReturn(userPoint);
        //when
        UserPoint resultUserPoint = pointService.viewPoint(id);

        //then
        assertThat(resultUserPoint).isNotNull();
        assertThat(resultUserPoint.id()).isEqualTo(id);
        assertThat(resultUserPoint.point()).isEqualTo(point);
    }

    @Test
    public void 유저의_id와_point를_입력받으면_충전된_userPoint를_반환(){
        //given
        long id = 1L;
        long currentPoint = 1000L;
        long chargePoint = 1000L;
        long updatePoint = currentPoint + chargePoint;

        UserPoint existingUserPoint = UserPoint.builder()
                .id(id)
                .point(currentPoint)
                .build();

        UserPoint updatedUserPoint = UserPoint.builder()
                .id(id)
                .point(updatePoint)
                .build();

        PointHistory mockPointHistory = PointHistory.builder()
                .id(1L) // 임의의 값
                .userId(id)
                .amount(chargePoint)
                .type(TransactionType.CHARGE)
                .build();

        given(userPointRepository.selectById(id)).willReturn(existingUserPoint);
        given(userPointRepository.updatePointById(id, updatePoint)).willReturn(updatedUserPoint);
        given(pointHistoryRepository.insertHistory(id, chargePoint, TransactionType.CHARGE)).willReturn(mockPointHistory);

        //when
        UserPoint resultUserPoint = pointService.chargePoint(id, chargePoint);

        //then
        verify(userPointRepository).updatePointById(id, updatePoint);
        assertThat(resultUserPoint).isNotNull();
        assertThat(resultUserPoint.point()).isEqualTo(updatePoint);
    }

    @Test
    public void 충전포인트가_10_000포인트를_넘을_경우_예외_반환(){
        //given
        long id = 1L;
        long currentPoint = 1000L;
        long chargePoint = 12000L;

        UserPoint existingUserPoint = UserPoint.builder()
                .id(id)
                .point(currentPoint)
                .build();

        given(userPointRepository.selectById(id)).willReturn(existingUserPoint);


        //when
        Throwable thrown = catchThrowable(() -> pointService.chargePoint(id, chargePoint));

        //then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 포인트는 10,000이하여야 합니다.");
    }

    @Test
    public void 충전_포인트가_100_미만인_경우_예외를_반환한다(){
        //given
        long id = 1L;
        long currentPoint = 1000L;
        long chargePoint = 50L;

        UserPoint existingUserPoint = UserPoint.builder()
                .id(id)
                .point(currentPoint)
                .build();

        given(userPointRepository.selectById(id)).willReturn(existingUserPoint);


        //when
        Throwable thrown = catchThrowable(() -> pointService.chargePoint(id, chargePoint));

        //then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("충전 포인트는 100이상이여야 합니다.");

    }

    @Test
    public void 유저의_id와_point를_입력받으면_사용된_userPoint를_반환(){
        //given
        long id = 1L;
        long currentPoint = 3000L;
        long usePoint = 1000L;
        long updatePoint = currentPoint - usePoint;

        UserPoint existingUserPoint = UserPoint.builder()
                .id(id)
                .point(currentPoint)
                .build();

        UserPoint updatedUserPoint = UserPoint.builder()
                .id(id)
                .point(updatePoint)
                .build();

        PointHistory mockPointHistory = PointHistory.builder()
                .id(1L) // 임의의 값
                .userId(id)
                .amount(usePoint)
                .type(TransactionType.CHARGE)
                .build();

        given(userPointRepository.selectById(id)).willReturn(existingUserPoint);
        given(userPointRepository.updatePointById(id, updatePoint)).willReturn(updatedUserPoint);
        given(pointHistoryRepository.insertHistory(id, usePoint, TransactionType.USE)).willReturn(mockPointHistory);

        //when
        UserPoint resultUserPoint = pointService.usePoint(id, usePoint);

        //then
        verify(userPointRepository).updatePointById(id, updatePoint);
        assertThat(resultUserPoint).isNotNull();
        assertThat(resultUserPoint.point()).isEqualTo(updatePoint);
    }

    @Test
    public void 포인트_사용_최소값은_100_이상_10_000이하여야_한다(){
        //given
        long id = 1L;
        long currentPoint = 3000L;
        long usePoint = 90L;

        UserPoint existingUserPoint = UserPoint.builder()
                .id(id)
                .point(currentPoint)
                .build();

        given(userPointRepository.selectById(id)).willReturn(existingUserPoint);

        //when
        Throwable thrown = catchThrowable(() -> pointService.usePoint(id, usePoint));

        //then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트 사용 최소값은 100 이상 10,000이하여야 합니다.");
    }

    @Test
    public void 포인트_사용시_잔고보다_많은_포인트를_사용할경우_예외_반환(){
        //given
        long id = 1L;
        long currentPoint = 3000L;
        long usePoint = 5000L;

        UserPoint existingUserPoint = UserPoint.builder()
                .id(id)
                .point(currentPoint)
                .build();

        given(userPointRepository.selectById(id)).willReturn(existingUserPoint);

        //when
        Throwable thrown = catchThrowable(() -> pointService.usePoint(id, usePoint));

        //then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용하려는 포인트가 잔고보다 많습니다.");
    }

}