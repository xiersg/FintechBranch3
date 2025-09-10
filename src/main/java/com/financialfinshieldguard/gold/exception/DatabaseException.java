package com.financialfinshieldguard.gold.exception;


import com.financialfinshieldguard.gold.constants.user.ErrorEnum;

public class DatabaseException extends RuntimeException {
    private final int code;

    public DatabaseException(String message) {
        super(message);
        this.code = ErrorEnum.DATABASE_ERROR.getCode();
    }

    public DatabaseException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.code = errorEnum.getCode();
    }

    public DatabaseException(ErrorEnum errorEnum, String message) {
        super(message);

        this.code = errorEnum.getCode();
    }

    public int getCode() {
        return code;
    }

}
