# SPRING PLUS





### 1-1. 문제 인식 및 정의
- /todos API를 호출하여 할 일을 저장하려고 할 때 다음과 같은 에러가 발생합니다
```
jakarta.servlet.ServletException: Request processing failed:
org.springframework.orm.jpa.JpaSystemException:
could not execute statement [Connection is read-only.
Queries leading to data modification are not allowed]
```
- 상단에 @Transactional(readOnly = true)어노테이션으로 인하여 DB 연결이 read-only 상태이기 때문에 INSERT, UPDATE와 같은 쓰기 작업이 차단되어 발생한 오류입니다.
- 해당 API는 데이터를 저장하는 쓰기 작업이므로 트랜잭션이 read-write 모드로 설정되어 있어야 합니다.


### 1-1. 해결 방안
- @Transactional을 서비스 메서드에 올바르게 적용
  - saveTodo() 같이 수정이 필요한 메서드에 @Transactional을 추가하여 쓰기 가능한 트랜잭션을 활성화


### 1-1. 해결 완료
```
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);
        ...
        Todo savedTodo = todoRepository.save(newTodo);
        ...
    }
}
```
- saveTodo()가 실행될 때 Spring이 트랜잭션을 열고, 쓰기 작업도 허용되기 때문에 정상적으로 할 일이 저장됩니다.

<br>
<br>


### 1-2. 문제 인식 및 정의
- 현재 서비스에서 JWT를 발급할 , 사용자 식별 정보로 userId, email, userRole만 포함하고 있으며 프론트엔드 화면에서 nickname을 함께 표시하고자 하는 요구사항이 생겼습니다.
- 기존 JWT에는 nickname 정보가 포함되어 있지 않기 때문에 클라이언트는 서버에서 nickname을 다시 조회하거나 요청을 추가로 보내야 하는 불편이 있습니다.

### 1-2. 해결 방안
1. User 엔티티 및 요청 객체 수정
   - User 테이블에 nickname 컬럼을 추가
   - 닉네임은 중복 가능하고 별도의 제약은 두지 않음
   - 회원가입 및 응답 객체에 nickname 추가
2. JWT 토큰 생성 시 nickname 추가
   - JwtUtil.createToken() 메서드의 파라미터에 nickname 추가
   - .claim("nickname", nickname) 으로 JWT에 nickname 추가
3. JwtFilter에서 nickname 추출
4. AuthUser 클래스 수정
    - nickname 컬럼을 추가
5. 각종 응답값에 nickname 추가

### 1-2. 해결 완료
- User.nickname 일관되게 반영
- JWT 생성시 nickname 추가

<br>
<br>

### 1-3. 문제 인식 및 정의
- 기존의 할 일 목록 조회 API (/todos)에 기능추가
  - 날씨 조건으로 검색
  - 수정일 기간 검색

### 1-3. 해결 방안
- @GetMapping 이므로 날씨, 시작일, 종료일 모두 바디가 아닌 @RequestParam 값으로 각각 요청
- QueryDSL 사용하여 추후에 유지보수하기 용이함

<br>
<br>

### 1-4. 문제 인식 및 정의
- 존재하지 않는 할일인 경우 InvalidRequestException이 발생하도록 처리하고 있습니다.<br>
하지만 컨트롤러 테스트 메서드 todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다()는 예외가 발생하였음에도 불구하고 응답 코드로 HttpStatus.OK(200) 되어있습니다.

### 1-4. 해결 방안
```
// 변경 전
.andExpect(status().isOk())
.andExpect(jsonPath("$.status").value(HttpStatus.OK.name()))
.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
//변경 후 
.andExpect(status().isBadRequest())
.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
.andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
 ```
- InvalidRequestException 처리에 따라 반환되는 HTTP 상태 코드 400 (BAD_REQUEST) 와 일치하도록 수정

### 1-5. 문제 인식 및 정의
- 현재 AOP 클래스인 AdminAccessLoggingAspect는 @After 어노테이션으로 UserController의 getUser 메소드 실행 시점에만 동작하도록 설정되어 있습니다.<br>
 하지만 여기서의 요구하는 의도는 changeUserRole() 메소드 실행 시점에서 관리자가 접근로그를 남기려고 합니다.<br>
 AOP 포인트컷이 잘못 설정되어 있어 원하는 시점에 로그가 기록되지 않는 문제가 있었습니다.

### 1-5. 해결 방안
- AOP 포인트컷 수정
```
// 변경 전
@After("execution(* org.example.expert.domain.user.controller.UserController.getUser(..))")
// 변경 후
@Before("execution(* org.example.expert.domain.user.controller.UserController.getUser(..))")
```

<br>
<br>

### 2-6. 문제 인식 및 정의
- 할 일을 작성하여 저장시 유저가 자동적으로 담당자로 등로되어야 합니다. 하지만 Manager 엔티티가 Todo와 연관 관계를 맺고 있음에도 불구하고 Todo 저장 시 Manager는 저장되지 않아 문제가 발생했습니다.

### 2-6. 해결 방안
- CascadeType.PERSIST 적용
  - @OneToMany(mappedBy = "todo", cascade = CascadeType.PERSIST)를 통해 Todo 저장 시 함께 등록된 Manager 엔티티도 함께 저장

### 2-6. 해결 완료
```
@OneToMany(mappedBy = "todo", cascade = CascadeType.PERSIST)
private List<Manager> managers = new ArrayList<>();
```

<br>
<br>

### 2-7. 문제 인식 및 정의
- N+1 문제
  - CommentController.getComments() API 호출 시 각 Comment 마다 User를 별도 쿼리로 조회하여 N+1 문제 발생

### 2-7 해결 방안
```
JOIN FETCH : 연관 객체도 한 번의 쿼리로 즉시 로딩
 - 단점 : 페이징이 불가능 한 경우도 있음
EntityGraph : 간결한 문법으로 fetch join 처리
 - 단점 : 복잡한 쿼리에서는 사용이 불가능
```

### 2-7. 해결 완료
```
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);
```
- JOIN FETCH 를 사용하여 Comment 와 연관된 User 를 한 번의 쿼리로 함께 조회
 
<br>
<br>

### 2-8. 문제 인식 및 정의
- TodoService.getTodo 메소드 내 todoRepository.findByIdWithUser(todoId) 호출 시 기존 JPQL로 작성된 쿼리가 N+1 문제를 발생시킬 수 있습니다.

### 2-8 해결 방안
- QueryDSL 의존성 추가
```
    implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
    annotationProcessor "com.querydsl:querydsl-apt:5.1.0:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
```
- JPAQueryFactory 빈을 등록하여 QueryDSL 쿼리를 사용할 수 있도록 설정
```
@Configuration
public class QueryDslConfig {

    private final EntityManager entityManager;

    public QueryDslConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```
- QueryDSL을 사용하여 구현할 메소드
```
public interface TodoCustomRepository {
Optional<Todo> findByIdWithUser(Long todoId);
}
```
- TodoCustomRepository 인터페이스를 구현
- JPAQueryFactory를 사용하여 Todo와 User를 fetchJoin()으로 함께 조회
```
public class TodoCustomRepositoryImpl implements TodoCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    public TodoCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        // Q클래스 인스턴스 생성
        // todo와 user 필드 접근 ?
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        // QueryDSL 쿼리 작성: todo와 user를 fetchJoin하여 함께 조회
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user) 
                .fetchJoin()              
                .where(todo.id.eq(todoId)) 
                .fetchOne());             
    }
}
```
- JpaRepository를 상속받는 TodoRepository 인터페이스에 TodoCustomRepository 인터페이스를 추가로 상속

```
public interface TodoRepository extends JpaRepository<Todo, Long>, TodoCustomRepository {
```
### 2-8. 해결 완료
- Todo를 조회할 때 연관된 User 정보도 fetchJoin을 통해 한번의 쿼리로 가져오게되어 N+1 문제를 해결

