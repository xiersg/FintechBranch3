package com.financialfinshieldguard.gold.exception;


import com.financialfinshieldguard.gold.constants.user.ErrorEnum;

public class UserException extends RuntimeException {
    private final int code;

    public UserException(String message) {
        super(message);
        this.code = ErrorEnum.SYSTEM_ERROR.getCode();
    }

    public UserException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.code = errorEnum.getCode();
    }

    public UserException(ErrorEnum errorEnum, String message) {
        super(message);

        this.code = errorEnum.getCode();
    }

    public int getCode() {
        return code;
    }
}
