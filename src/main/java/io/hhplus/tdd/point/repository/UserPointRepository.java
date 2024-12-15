package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entity.UserPoint;
import org.springframework.stereotype.Repository;

@Repository
public class UserPointRepository {

    private final UserPointTable userPointTable;

    public UserPointRepository(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    public UserPoint selectById(long id) {

        long resultId = userPointTable.selectById(id).id();
        long resultPoint = userPointTable.selectById(id).point();

        return UserPoint.findPoint(resultId, resultPoint);
    }
}
