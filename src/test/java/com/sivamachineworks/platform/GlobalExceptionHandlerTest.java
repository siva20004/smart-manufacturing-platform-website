package com.sivamachineworks.platform;

import com.sivamachineworks.platform.shared.exception.BaseException;
import com.sivamachineworks.platform.shared.exception.ErrorCode;
import com.sivamachineworks.platform.shared.exception.GlobalExceptionHandler;
import com.sivamachineworks.platform.shared.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleBaseException() {
        BaseException ex = new BaseException(ErrorCode.NOT_FOUND, "Resource not found");
        ResponseEntity<ErrorResponse> response = handler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().error().message()).isEqualTo("Resource not found");
        assertThat(response.getBody().correlationId()).isNotNull();
    }
}
