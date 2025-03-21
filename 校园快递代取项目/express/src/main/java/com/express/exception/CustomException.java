package com.express.exception;

import com.express.domain.ResponseResult;
import com.express.domain.enums.ResponseErrorCodeEnum;

/**
 * 默认异常处理
 */
public class CustomException extends RuntimeException {
    private Integer code;

    public CustomException(ResponseErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMsg());
        this.code = errorCodeEnum.getCode();
    }

    public CustomException(ResponseResult result) {
        super(result.getMsg());
        this.code = result.getCode();
    }

    public CustomException(Integer code , String info) {
        super(info);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
