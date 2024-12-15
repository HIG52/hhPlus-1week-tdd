package io.hhplus.tdd.point.entity;

import lombok.Builder;

@Builder
public record UserPoint(
        long id,
        long point,
        long updateMillis
    )
{
    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public static UserPoint findPoint(long id, long amount) {
        return new UserPoint(id, amount, System.currentTimeMillis());
    }
}
