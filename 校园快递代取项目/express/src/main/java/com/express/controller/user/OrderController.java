package com.express.controller.user;

import com.express.common.constant.SessionKeyConstant;
import com.express.domain.ResponseResult;
import com.express.domain.bean.OrderInfo;
import com.express.domain.bean.SysUser;
import com.express.domain.enums.PaymentStatusEnum;
import com.express.domain.enums.ResponseErrorCodeEnum;
import com.express.exception.CustomException;
import com.express.service.OrderInfoService;
import com.express.service.OrderPaymentService;
import com.express.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 订单
 */
@Controller
@RequestMapping("/order")
@PreAuthorize("hasRole('ROLE_USER')")
public class OrderController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private OrderPaymentService paymentService;

    /**
     * 支付宝支付方式
     * @param money 支付金额
     */
    @PostMapping("/alipay")
    public String paymentAlipay(Double money, HttpSession session, HttpServletResponse response, @AuthenticationPrincipal SysUser sysUser, ModelMap map) {
        OrderInfo orderInfo = (OrderInfo)session.getAttribute(SessionKeyConstant.SESSION_LATEST_EXPRESS);
        if(orderInfo == null || money == null) {
            throw new CustomException(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        // 金额保留两位
        money = (double) (Math.round(money * 100)) / 100;
        // 生成订单 & 订单支付
        ResponseResult result1 = orderInfoService.createOrder(orderInfo, money, sysUser.getId());
        if(result1.getCode() != ResponseErrorCodeEnum.SUCCESS.getCode()) {
            throw new CustomException(result1);
        }
        String orderId = (String)result1.getData();
        paymentService.updateStatus(orderId, PaymentStatusEnum.TRADE_SUCCESS, orderId + "alipay");
        map.put("frontName", sysUserService.getFrontName(sysUser));
        return "user/paymentResult";
    }

}
