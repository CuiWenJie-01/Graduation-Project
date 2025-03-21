package com.express.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.express.domain.ResponseResult;
import com.express.domain.bean.OrderEvaluate;
import com.express.domain.enums.SysRoleEnum;

public interface OrderEvaluateService extends IService<OrderEvaluate> {

    boolean initOrderEvaluate(String orderId);

    boolean changEvaluateStatus(String orderId, boolean isOpen);
    /**
     * 能否评价订单
     * @param roleEnum 只支持用户和配送员
     */
    boolean canEvaluate(String orderId, String userId, SysRoleEnum roleEnum);

    ResponseResult userEvaluate(String orderId, String userId, double score, String text);

    ResponseResult courierEvaluate(String orderId, String courierId, double score, String text);

    long countEvaluate(String userId, SysRoleEnum roleEnum);
}
