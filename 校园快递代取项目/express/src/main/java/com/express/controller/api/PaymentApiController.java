package com.express.controller.api;

import com.express.common.util.StringUtils;
import com.express.domain.ResponseResult;
import com.express.domain.bean.SysUser;
import com.express.domain.enums.ResponseErrorCodeEnum;
import com.express.service.OrderInfoService;
import com.express.service.OrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API支付接口
 */
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentApiController {
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private OrderPaymentService orderPaymentService;

    /**
     * 手动同步支付状态
     */
    @PostMapping("/sync")
    public ResponseResult syncPaymentStatus(String orderId, @AuthenticationPrincipal SysUser sysUser) {
        if(StringUtils.isBlank(orderId)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        if(!orderInfoService.isUserOrder(orderId, sysUser.getId())) {
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }

        return orderPaymentService.syncPaymentInfo(orderId);
    }
}
