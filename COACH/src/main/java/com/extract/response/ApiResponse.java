package com.extract.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.extract.exception.CException;

public class ApiResponse {
    public static <T, R> ResponseEntity<T> newOKResponse(R result) {
        SuccessfulResponse<R> response = new SuccessfulResponse<>(result);
        return new ResponseEntity<>((T) response, HttpStatus.OK);
    }

public static <T, R> ResponseEntity<T> newBadResponse(CException e) {
    FailedResponse<R> response = new FailedResponse<R>(e.getErrorCode(), e.getErrorDesc());
    if (e instanceof CException) {
        return new ResponseEntity<>((T) response, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>((T) response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
