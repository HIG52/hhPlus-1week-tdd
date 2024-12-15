package io.hhplus.tdd.point;

import io.hhplus.tdd.point.entity.UserPoint;
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
    @DisplayName("유저의 id값을 입력받으면 userPoint를 반환")
    public void viewPointTest(){  //유저의 포인트가 정상적으로 반환이 되는지 확인
        //given
        Long id = 1L;
        Long amount = 1000L;
        UserPoint userPoint = UserPoint.builder()
                .id(id)
                .point(amount)
                .build();

        given(userPointRepository.selectById(id)).willReturn(userPoint);
        //when
        UserPoint resultUserPoint = pointService.viewPoint(id);

        //then
        assertThat(resultUserPoint).isNotNull();
        assertThat(resultUserPoint.id()).isEqualTo(id);
        assertThat(resultUserPoint.point()).isEqualTo(amount);
    }

}