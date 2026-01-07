package com.example.library_management_system.web.exception;

import com.example.library_management_system.web.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 登录失败：用户名或密码错误
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ApiResponse<Void> handleBadCredentials(Exception e) {
        return ApiResponse.error(401, "用户名或密码错误");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<Void> handleNotFound(EntityNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<Void> handleValidation(Exception e) {
        String msg = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException ex) {
            if (ex.getBindingResult().getFieldError() != null) {
                msg = ex.getBindingResult().getFieldError().getField() + ": " + ex.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        if (e instanceof BindException ex) {
            if (ex.getBindingResult().getFieldError() != null) {
                msg = ex.getBindingResult().getFieldError().getField() + ": " + ex.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        return ApiResponse.error(400, msg);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ApiResponse<Void> handleBadRequest(RuntimeException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return ApiResponse.error(400, "请求体解析失败");
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e) {
        e.printStackTrace();
        return ApiResponse.error(500, "服务器内部错误: " + e.getClass().getSimpleName());
    }
}
