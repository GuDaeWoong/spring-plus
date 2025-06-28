package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;


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
}