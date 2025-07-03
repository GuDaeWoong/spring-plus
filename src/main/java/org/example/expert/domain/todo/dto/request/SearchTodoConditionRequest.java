package org.example.expert.domain.todo.dto.request;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class SearchTodoConditionRequest {
    private String keyword;
    private String Nickname;
    private LocalDate startDate;
    private LocalDate endDate;
}
