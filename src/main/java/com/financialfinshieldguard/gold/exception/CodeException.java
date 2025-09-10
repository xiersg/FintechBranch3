package com.financialfinshieldguard.gold.exception;


import com.financialfinshieldguard.gold.constants.user.ErrorEnum;

public class CodeException extends RuntimeException {

    private final int code;

    public CodeException(String message) {
        super(message);
        this.code = ErrorEnum.SYSTEM_ERROR.getCode();
    }

    public CodeException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.code = errorEnum.getCode();
    }

    public CodeException(ErrorEnum errorEnum, String message) {
        super(message);

        this.code = errorEnum.getCode();
    }

    public int getCode() {
        return code;
    }

}
