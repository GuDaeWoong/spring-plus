package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.request.SearchTodoConditionRequest;
import org.example.expert.domain.todo.dto.response.SearchTodoResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TodoCustomRepositoryImpl implements TodoCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;

    public TodoCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {

        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user,user)
                .fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne());
    }

    @Override
    public Page<SearchTodoResponse> searchByCondition(
            SearchTodoConditionRequest conditionRequest,
            Pageable pageable) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;
        QComment comment = QComment.comment;
        QManager manager = QManager.manager;

        List<SearchTodoResponse> content = jpaQueryFactory
                .select(Projections.constructor(SearchTodoResponse.class,
                        todo.title,
                        manager.id.countDistinct(),
                        comment.id.countDistinct()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user,user)
                .leftJoin(todo.comments, comment)
                .where(
                        titleContains(conditionRequest.getKeyword()),
                        managerNicknameContains(conditionRequest.getNickname()),
                        createdDateBetween(conditionRequest.getStartDate(), conditionRequest.getEndDate())
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(todo.id.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(
                        titleContains(conditionRequest.getKeyword()),
                        managerNicknameContains(conditionRequest.getNickname()),
                        createdDateBetween(conditionRequest.getStartDate(), conditionRequest.getEndDate())
                )
                .fetchOne();

        long totalCount = 0;
        if (total != null) {
            totalCount = total;
        }

        return new PageImpl<>(content, pageable, totalCount);
    }

    private BooleanExpression titleContains(String keyword) {
        if (keyword != null || !keyword.isBlank()) {
            return null;
        }
        return QTodo.todo.title.containsIgnoreCase(keyword);
    }

    private BooleanExpression managerNicknameContains(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return null;
        }
        return QManager.manager.user.nickname.containsIgnoreCase(nickname);
    }

    private BooleanExpression createdDateBetween(LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return QTodo.todo.createdAt.between(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        } else if (start != null) {
            return QTodo.todo.createdAt.goe(start.atStartOfDay());
        } else if (end != null) {
            return QTodo.todo.createdAt.loe(end.plusDays(1).atStartOfDay());
        }
        return null;
    }
}