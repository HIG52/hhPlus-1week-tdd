package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.service.PointHistoryService;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.type.TransactionType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    @InjectMocks
    private PointHistoryService pointHistoryService;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointService pointService;

    private static List<PointHistory> PointHistoryList() {
        PointHistory pointHistoryList1 = PointHistory.builder()
                .id(1L)
                .userId(1L)
                .amount(1000L)
                .type(TransactionType.CHARGE)
                .updateMillis(System.currentTimeMillis())
                .build();

        PointHistory pointHistoryList2 = PointHistory.builder()
                .id(2L)
                .userId(1L)
                .amount(1000L)
                .type(TransactionType.CHARGE)
                .updateMillis(System.currentTimeMillis())
                .build();

        PointHistory pointHistoryList3 = PointHistory.builder()
                .id(3L)
                .userId(1L)
                .amount(500L)
                .type(TransactionType.USE)
                .updateMillis(System.currentTimeMillis())
                .build();

        return List.of(pointHistoryList1, pointHistoryList2, pointHistoryList3);
    }

    @Test
    @DisplayName("특정 유저의 id를 입력받으면 해당 유저의 PointHistory List를 반환")
    public void listHistoriesTest(){
        //given
        List<PointHistory> pointHistoryList = PointHistoryList();
        long userId = 1L;

        given(pointHistoryRepository.listHistories(userId)).willReturn(pointHistoryList);

        //when
        List<PointHistory> resultPointHistoryList = pointHistoryService.listHistories(userId);

        //then
        verify(pointHistoryRepository).listHistories(userId);
        assertThat(resultPointHistoryList)
                .extracting("userId", "amount", "type")
                .contains(
                        tuple(1L, 1000L, TransactionType.CHARGE),
                        tuple(1L, 1000L, TransactionType.CHARGE),
                        tuple(1L, 500L, TransactionType.USE)
                );
    }

}