package com.extract.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.extract.response.ApiResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final int UNKNOWN_SYSTEM_ERROR = 10102;
    private static final int BODY_PARAMETER_ERROR = 10103;

    @ExceptionHandler(value = Throwable.class)
    @ResponseBody
    public Object jsonErrorHandler(Throwable e) {
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            return getResponse(exception);
        } else if (e instanceof HttpMessageNotReadableException) {
            HttpMessageNotReadableException exception = (HttpMessageNotReadableException) e;
            return getResponse(exception);
        } else if (e instanceof ConstraintViolationException) {
            ConstraintViolationException exception = (ConstraintViolationException) e;
            return getResponse(exception);
        } else if (e instanceof CException) {
            CException exception = (CException) e;
            return ApiResponse.newBadResponse(exception);
        } else {
            return getResponse(e);
        }
    }

    private Object getResponse(ConstraintViolationException exception) {
        String field = exception.getConstraintViolations().iterator().next().getPropertyPath().toString();
        String errorMessage = exception.getMessage();
        log.error("GlobalExceptionHandler handle an ConstraintViolationException. field is {}, errorMessage is {}", field, errorMessage);
        CException paramException = new CException(BODY_PARAMETER_ERROR, errorMessage);
        return ApiResponse.newBadResponse(paramException);
    }

    private Object getResponse(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        ObjectError error = bindingResult.getAllErrors().get(0);
        if (!(error instanceof FieldError)) {
            return null;
        }
        FieldError fieldError = (FieldError) error;
        String objectName = fieldError.getObjectName();
        String field = fieldError.getField();
        String errorMessage = fieldError.getField() + " " + error.getDefaultMessage();
        log.error("GlobalExceptionHandler handle an MethodArgumentNotValidException. field is {}, errorMessage is {}", objectName + "." + field, errorMessage);
        CException paramException = new CException(BODY_PARAMETER_ERROR, errorMessage);
        return ApiResponse.newBadResponse(paramException);
    }

    private Object getResponse(HttpMessageNotReadableException exception) {
        CException cException = new CException(BODY_PARAMETER_ERROR, "request body to Object error");
        log.error("GlobalExceptionHandler handle an HttpMessageNotReadableException. parse request body to Object error");
        return ApiResponse.newBadResponse(cException);
    }

    private Object getResponse(Throwable e) {
        CException cException = new CException(UNKNOWN_SYSTEM_ERROR, "system error: " + e.getMessage());
        log.error("GlobalExceptionHandler handle an Throwable", e);
        return ApiResponse.newBadResponse(cException);
    }
    
}
