package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.PointHistory;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.service.PointHistoryServiceImpl;
import io.hhplus.tdd.point.service.PointService;
import io.hhplus.tdd.point.type.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    @InjectMocks
    private PointHistoryServiceImpl pointHistoryServiceImpl;

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
    public void 특정_유저의_id를_입력받으면_해당_유저의_PointHistory_List를_반환(){
        //given
        List<PointHistory> pointHistoryList = PointHistoryList();
        long userId = 1L;

        given(pointHistoryRepository.listHistories(userId)).willReturn(pointHistoryList);

        //when
        List<PointHistory> resultPointHistoryList = pointHistoryServiceImpl.listHistories(userId);

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

    @Test
    public void 유저가_포인트를_충전_사용할때_PointHistory를_저장후_PointHistory를반환한다(){
        //given
        long userId = 1L;
        long usePoint = 1000L;


        PointHistory pointHistory = PointHistory.builder()
                .userId(userId)
                .amount(usePoint)
                .type(TransactionType.USE)
                .updateMillis(System.currentTimeMillis())
                .build();

        given(pointHistoryRepository.insertHistory(userId, usePoint, TransactionType.USE)).willReturn(pointHistory);

        //when
        PointHistory insertPointHistory = pointHistoryServiceImpl.insertPointHistory(userId, usePoint, TransactionType.USE);

        //then
        verify(pointHistoryRepository).insertHistory(userId, usePoint, TransactionType.USE);
        assertThat(insertPointHistory).isNotNull();
        assertThat(insertPointHistory.userId()).isEqualTo(userId);
        assertThat(insertPointHistory.amount()).isEqualTo(usePoint);
        assertThat(insertPointHistory.type()).isEqualTo(TransactionType.USE);
    }


}