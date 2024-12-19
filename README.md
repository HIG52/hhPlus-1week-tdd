# 1주차 과제 

## 요구사항 

- PATCH  `/point/{id}/charge` : 포인트를 충전한다.
- PATCH `/point/{id}/use` : 포인트를 사용한다.
- GET `/point/{id}` : 포인트를 조회한다.
- GET `/point/{id}/histories` : 포인트 내역을 조회한다.
- 잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.
- 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.

### 분석 및 정책
1. 포인트 충전 기능
   1. 포인트 충전시 결과값이 10,000 초과일경우 요청이 실패한다.
   2. 포인트 충전시 최소값이 100 미만일경우 요청이 실패한다.
   3. 1번과 2번이 아닐경우 기존값에 요청값을 더하여 저장한다.
2. 포인트 사용 기능
   1. 포인트 사용 최소값은 100 이상 10,000이하여야 한다.
   2. 포인트 사용시 사용값이 기존값보다 크면 요청에 실패한다.
3. 포인트 조회 기능
4. 포인트 내역 조회 기능

### 동시성 이슈
1. 동일 유저
   1. 같은 유저의 요청은 순차적으로 이루어져야 한다.
2. 다른 유저
    1. 서로 다른 유저의 요청은 동시에 이루어져야 한다.

## STEP01`기본과제`

- 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등)
- 동시에 여러 요청이 들어오더라도 순서대로 (혹은 한번에 하나의 요청씩만) 제어될 수 있도록 리팩토링
- 동시성 제어에 대한 통합 테스트 작성


## STEP02`심화과제`

- 동시성 제어 방식에 대한 분석 및 보고서 작성 ( **README.md** )

## 필수사항
- 테스트 케이스의 작성 및 작성 이유를 주석으로 작성하도록 합니다.
- 프로젝트 내의 주석을 참고하여 필요한 기능을 작성해주세요.
- 분산 환경은 고려하지 않습니다.

# 동시성 제어 방식에 대한 분석 및 보고서

## 1. 문제 정의
### 1.1 동시성 문제란? 
   - 동시성 문제는 여러 스레드가 동일한 자원에 동시에 접근하거나 수정하려고 할 때 발생한다.
   - 데이터의 일관성이 깨진다 (Race Condition)
   - 한 스레드가 쓰기 작업을 하는 동안 다른 스레드가 같은 데이터를 읽거나 수정하려고 할 때 예측 불가능한 결과가 발생
### 1.2 요구사항 
   - 요청이 들어왔을 때 순서대로 처리해야 한다.
   - 일관성을 유지해야 한다.
   - 동일 사용자의 요청은 순차적으로 처리해야한다.
   - 다른 사용자의 요청은 동시에 처리해야 한다.

## 2. 동시성 제어 전략
### 2.1 요구사항 분석에 따른 사용
| 요구사항               | 사용할 동시성 제어 |
|--------------------|------------|
| 동일 사용자의 요청은 순차적 처리 | 사용자 ID별로 ReentrantLock을 적용       |
| 다른 사용자의 요청은 동시에 처리               | ConcurrentHashMap을 사용하여 사용자별 Lock 관리       |
### 2.2 동시성 문제 해결 방안
1. ReentrantLock
   1. 특징: 한 스레드가 같은 락을 여러 번 획득할 수 있다.
   2. 공정성 설정: 락의 공정성을 true로 설정하여 오래 기다린 스레드에게 우선순위를 부여한다.
   3. 사용 목적: 사용자 단위로 순차적으로 요청을 처리하기 위해 사용.

2. ConcurrentHashMap
   1. 특징: 동시 접근이 가능한 해시맵으로 동기화된 데이터 구조를 제공한다.
   2. 사용 목적: 사용자별로 ReentrantLock을 저장하고 관리하기 위해 사용.

## 3. 구현 예시
### 3.1 구현 예시
```
   //각 ID에 대한 락관리
   private final ConcurrentHashMap<Long, Lock> lockMap = new ConcurrentHashMap<>();
   
   private Lock getLock(long id) {
      return lockMap.computeIfAbsent(id, key -> new ReentrantLock(true)); //공정성 선입선출
   }
    
   public UserPoint chargePoint(long id, long point) {
        //id별로 락 생성
        Lock lock = getLock(id);
        lock.lock(); //락 획득

        try {
            //long currentPoint = viewPoint(id).point();
            long currentPoint = userPointRepository.selectById(id).point(); //불필요한 중첩락
            long chargePoint = point;
            long updatedPoint = currentPoint + chargePoint;

            chargePointException(chargePoint);
            UserPoint chargedUserPoint = userPointRepository.updatePointById(id, updatedPoint);

            PointHistory pointHistory = pointHistoryService.insertPointHistory(id, point, TransactionType.CHARGE);
            pointHistoryException(pointHistory);
            return chargedUserPoint;

        }finally {
            lock.unlock(); //락 해제
            
        }
    }
    
```
#### 3.1.1. 동시성 제어 방식 비교
| 방식                 | 장점                                   | 단점                          |
|--------------------|--------------------------------------|-----------------------------|
| Synchronized | 간단하게 임계영역을 설정 가능| 요청이 많아질수록 대기시간이 길어짐         |
| ReentrantLock | 락에 대한 세밀한 제어 가능 (공정성 설정 포함) | 명시적으로 락을 해제                 |
| ConcurrentHashMap | 동시 접근이 가능한 데이터 구조를 제공 | 로직 단위 동기화는 보장하지 않음(2개이상 연산) |

### 3.2 테스트 구현 예시
```
   ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
   CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
   
   List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>()); // 예외 기록
   List<String> successes = Collections.synchronizedList(new ArrayList<>()); // 성공 기록
   
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
   
   latch.await(); // 모든 스레드가 끝날 때까지 대기
   executorService.shutdown();           
```
#### 3.2.1. 동시성 테스트 도구 분석
| 동시성 제어 방식              | 장점                        | 단점                         |
|------------------------|---------------------------|----------------------------|
| ExecutorService        | 스레드 풀을 사용해 스레드 관리를 효율적으로 수행 | 스레드 풀 크기를 잘못 설정하면 성능 저하|
| CountDownLatch         | 여러 스레드의 작업 완료를 동기화하며 제어 가능 | 한 번 사용하면 재사용이 불가능|
| ReentrantReadWriteLock | 읽기와 쓰기 작업을 분리해 동시 접근을 최적화 | 쓰기 작업이 많으면 성능 저하 가능 |
| Semaphore              | 한정된 리소스를 여러 스레드가 공유하도록 제어 | 복잡한 로직에서는 관리가 어려울 수 있음 |
| AtomicInteger          | 단일 연산에 대해 원자적 연산을 보장     | 복합 연산에서는 동기화가 필요 |

## 5. 결론
ReentrantLock을 활용하여 동일 사용자 요청의 순차 처리를 보장하였고

ConcurrentHashMap을 통해 사용자별로 독립적인 동시 처리를 하였으며

공정성 옵션을 통해 오랫동안 기다린 요청이 우선 처리되도록 보장하였다.

이를 통해 포인트 시스템의 안정성과 성능을 동시에 확보하면서도 동시성 문제를 해결 하였다.


