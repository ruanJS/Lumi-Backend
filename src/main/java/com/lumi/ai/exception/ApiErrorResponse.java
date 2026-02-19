package com.lumi.ai.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private ErrorType type;
    private String title;
    private String detail;
    private String path;
    private List<FieldErrorResponse> errors;
}
