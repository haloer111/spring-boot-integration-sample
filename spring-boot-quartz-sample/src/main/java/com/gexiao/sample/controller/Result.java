package com.gexiao.sample.controller;

import java.util.function.Consumer;

/**
 * 统一返回对象
 */
public class Result<T> {
    /**
     * 状态码（业务状态码，默认非0为成功）
     */
    private int status = 0;
    /**
     * 成功状态
     */
    private boolean success = true;
    /**
     * 数据对象
     */
    private T data;
    /**
     * 描述信息
     */
    private String message;

    public Result() {
    }

    public Result(Consumer<Result> consumer) {
        try {
            consumer.accept(this);
        } catch (Exception e) {
            this.error(e);
            e.printStackTrace();
        }
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Result(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
        this.success = status >= 0;
    }

    /**
     * 操作成功
     */
    public static Result ok() {
        return ok(null);
    }

    /**
     * 操作成功
     */
    public static <T> Result ok(T data) {
        return ok("操作成功", data);
    }

    /**
     * 操作成功
     */
    public static <T> Result ok(String message, T data) {
        return new Result<T>().setSuccess(true).setMessage(message).setData(data);
    }

    /**
     * 快速构造失败返回对象
     */
    public static Result fail() {
        return fail("操作失败");
    }

    /**
     * 构造失败返回对象
     *
     * @param throwable 异常
     */
    public static Result fail(Exception throwable) {
        Result re = new Result();
        re.error(throwable);
        return re;
    }

    /**
     * 构建快速返回失败对象
     *
     * @param message 失败信息
     */
    public static Result fail(String message) {
        return fail(-1, message);
    }


    /**
     * 构建快速返回失败对象
     *
     * @param status  业务状态码
     * @param message 描述信息
     */
    public static Result fail(int status, String message) {
        return new Result().setSuccess(false).setStatus(status).setMessage(message);
    }


    /**
     * 构造失败返回并设置业务状态码
     *
     * @param status    业务状态码
     * @param exception 异常
     */
    public static Result fail(int status, Exception exception) {
        return fail(exception).setStatus(status);
    }


    /**
     * @param data
     * @param message
     * @return
     */
    public static <T> Result ok(T data, String message) {
        Result<T> re = new Result<>();
        re.setSuccess(true).setData(data);
        re.setMessage(message);
        return re;
    }

    public void error(Exception e) {
        error(-1, e);
    }

    public void error(int status, Exception e) {
        this.success = false;
        this.status = status;
        if (e.getCause() == null)
            message = e.getMessage();
        else
            message = e.getCause().getMessage();
    }


    public String getMessage() {
        return message;
    }

    public Result<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public long getStatus() {
        return status;
    }

    public Result<T> setStatus(int status) {
        this.status = status;
        return this;
    }

    public Result<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public Result<T> set(T data, String msg) {
        return setData(data).setMessage(msg);
    }
}
