package com.jtkj.library.infrastructure.network.exception;

public class HttpTimeException extends RuntimeException {

    private static final int NO_DATA = 0x2;

    public HttpTimeException(int resultCode) {
        this(getApiExceptionMessage(resultCode));
    }

    public HttpTimeException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * 转换错误数据
     */
    private static String getApiExceptionMessage(int code) {
        String message;
        switch (code) {
            case NO_DATA:
                message = "no data";
                break;
            default:
                message = "error";
                break;
        }
        return message;
    }
}

