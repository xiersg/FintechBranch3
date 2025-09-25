package com.financialfinishieldguard.data.common;

import com.financialfinishieldguard.constants.StatusConstant;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)//开启链式编程
public class Result<T> {
    private int code;

    private String msg;

    private T data;

    public static <T> Result<T> OK(){
        Result<T> r = new Result<>();

        return r.setCode(StatusConstant.OK);
    }

    public static <T> Result<T> OK(T data){
        Result<T> r = new Result<>();

        return r.setCode(StatusConstant.OK).setData(data);
    }

    public static <T> Result<T> ValidError(String msg){
        Result<T> r = new Result<>();

        return r.setCode(StatusConstant.BAD_REQUEST).setMsg(msg);
    }

    public static <T> Result<T> DatabaseError(String msg){
        Result<T> r = new Result<>();

        return r.setCode(StatusConstant.INTERNAL_SERVER_ERROR).setMsg(msg);
    }

    public static <T> Result<T> ServerError(String msg){
        Result<T> r = new Result<>();

        return r.setCode(StatusConstant.INTERNAL_SERVER_ERROR).setMsg(msg);
    }

    public static <T> Result<T> UserError(int code, String msg){
        Result<T> r = new Result<>();

        return r.setCode(code).setMsg(msg);
    }


}
