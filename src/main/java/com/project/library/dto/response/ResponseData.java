package com.project.library.dto.response;

public class ResponseData<T>{

    private final int status;
    private final String message;
    private T data;

    public ResponseData(int status, String message) { // Còn lại
        this.status = status;
        this.message = message;
    }

    public ResponseData(int status, String message, T data) { // GET, POST
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

    public T getData() {
        return data;
    }
}
