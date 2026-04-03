package com.tarnof.enjoyrestapi.handlers;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int status;
    private String error;
    private Instant timestamp;
    private String message;
    private String path;
    private List<String> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, Instant timestamp, String message, String path, List<String> errors) {
        this.status = status;
        this.error = error;
        this.timestamp = timestamp;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponse that = (ErrorResponse) o;
        return status == that.status
                && Objects.equals(error, that.error)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(message, that.message)
                && Objects.equals(path, that.path)
                && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, error, timestamp, message, path, errors);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", path='" + path + '\'' +
                ", errors=" + errors +
                '}';
    }

    public static class ErrorResponseBuilder {
        private int status;
        private String error;
        private Instant timestamp;
        private String message;
        private String path;
        private List<String> errors;

        public ErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder error(String error) {
            this.error = error;
            return this;
        }

        public ErrorResponseBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponseBuilder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(status, error, timestamp, message, path, errors);
        }
    }
}
