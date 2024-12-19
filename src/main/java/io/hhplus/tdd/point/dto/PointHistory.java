package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.type.TransactionType;
import lombok.Builder;

@Builder
public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
