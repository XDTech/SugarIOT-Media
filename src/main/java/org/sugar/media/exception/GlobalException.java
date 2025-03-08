package org.sugar.media.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.extra.validation.BeanValidationResult;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.enums.ResponseEnum;

import java.util.ConcurrentModificationException;
import java.util.List;


/**
 * 全局异常处理
 * https://www.coderacademy.online/article/springbootcommonresponse.html
 */
// TODO:根据项目风格修改异常拦截返回值
@ControllerAdvice
public class GlobalException extends Exception {
    // 全局异常拦截（拦截项目中的所有异常）


    // 表单验证异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseBody
    public ResponseBean handleValidationExceptions(MethodArgumentNotValidException ex) {
        ex.printStackTrace();
        List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
        if (!allErrors.isEmpty()) {
            ObjectError error = allErrors.get(0);
            String description = error.getDefaultMessage();
            return ResponseBean.createResponseBean(ResponseEnum.Fail.getCode(), description);
        }
        return ResponseBean.createResponseBean(ResponseEnum.Fail.getCode(), "validation failed");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public String handlerConstraintViolationException(ConstraintViolationException ex) {
        ex.printStackTrace();
        return ex.getMessage();
    }

    // 未登录异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    @ResponseBody
    public String handleNotLoginExceptions(NotLoginException ex) {
        return "";
    }

    /**
     * 通用异常处理(用于处理不可预知的异常)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exceptionHandler(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Network error");
    }

}