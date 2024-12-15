package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
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

    @Test
    @DisplayName("유저의 id를 입력받으면 userPoint를 반환")
    public void viewPointTest(){  //유저의 포인트가 정상적으로 반환이 되는지 확인
        //given
        Long id = 1L;
        Long point = 1000L;
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
    @DisplayName("유저의 id, point를 입력받으면 업데이트된 userPoint를 반환")
    public void chargePointTest(){
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


        given(userPointRepository.selectById(id)).willReturn(existingUserPoint);
        given(userPointRepository.updatePointById(id, updatePoint)).willReturn(updatedUserPoint);

        //when
        UserPoint resultUserPoint = pointService.chargePoint(id, chargePoint);

        //then
        verify(userPointRepository).updatePointById(id, updatePoint);
        assertThat(resultUserPoint).isNotNull();
        assertThat(resultUserPoint.point()).isEqualTo(updatePoint);
    }



}