package com.spring.code.demo.dto.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class ResponseSuccess extends ResponseEntity<ResponseSuccess.PayLoad> {

    // Mô tả dữ liệu trả về cho các API PUT, PATCH, DELETE
    public ResponseSuccess(HttpStatusCode status, String message) {
        super(new PayLoad(status.value(), message), HttpStatus.OK);
    }
    // GET, POST
    public ResponseSuccess(HttpStatusCode status, String message, Object data) {
        super(new PayLoad(status.value(), message, data), HttpStatus.OK);
    }
    public static class PayLoad {
        private final int status;
        private final String message;
        private Object data;

        public PayLoad(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public PayLoad(int status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}
