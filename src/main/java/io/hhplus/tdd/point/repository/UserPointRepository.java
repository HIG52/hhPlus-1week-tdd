package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.dto.UserPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserPointRepository {

    @Autowired
    private UserPointTable userPointTable;

    public UserPoint selectById(long id) {

        long resultId = userPointTable.selectById(id).id();
        long resultPoint = userPointTable.selectById(id).point();

        return new UserPoint(resultId, resultPoint, System.currentTimeMillis());
    }

    public UserPoint updatePointById(long id, long point) {
        UserPoint resultUserPoint = userPointTable.insertOrUpdate(id, point);

        return new UserPoint(resultUserPoint.id(), resultUserPoint.point(), System.currentTimeMillis());
    }
}
