package com.express.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.express.common.util.StringUtils;
import com.express.domain.ResponseResult;
import com.express.domain.bean.SysUser;
import com.express.domain.bean.UserFeedback;
import com.express.domain.enums.FeedbackStatusEnum;
import com.express.domain.enums.FeedbackTypeEnum;
import com.express.domain.enums.ResponseErrorCodeEnum;
import com.express.domain.vo.BootstrapTableVO;
import com.express.domain.vo.UserFeedbackVO;
import com.express.service.OrderInfoService;
import com.express.service.UserFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * API 反馈接口
 */
@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackApiController {
    @Autowired
    private UserFeedbackService userFeedbackService;
    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 分页查询所有反馈记录
     * - 普通用户和配送员：个人记录
     * - 管理员：所有记录
     */
    @GetMapping("/list")
    public BootstrapTableVO<UserFeedbackVO> listSelfFeedback(@RequestParam(required = false, defaultValue = "1") Integer current,
                                                             @RequestParam(required = false, defaultValue = "10") Integer size,
                                                             String type, String status, String id,
                                                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                             @AuthenticationPrincipal SysUser sysUser) {
        Page<UserFeedback> page = new Page<>(current, size);
        OrderItem orderItem = new OrderItem();
        orderItem.setAsc(false);
        orderItem.setColumn("create_date");
        List<OrderItem> orders = new ArrayList<>();
        orders.add(orderItem);
        page.setOrders(orders);
        QueryWrapper<UserFeedback> wrapper = new QueryWrapper<>();

        Integer feedStatus = StringUtils.toInteger(status, -1);
        if(FeedbackStatusEnum.getByStatus(feedStatus) != null) {
            wrapper.eq("status", feedStatus);
        }

        Integer feedbackType1 = StringUtils.toInteger(type, -1);
        if(FeedbackTypeEnum.getByType(feedbackType1) != null) {
            wrapper.eq("type", feedbackType1);
        }

        if(StringUtils.isNotBlank(id)) {
            wrapper.eq("id", id);
        }
        if(startDate != null) {
            wrapper.ge("create_date", startDate);
        }
        if(endDate != null) {
            wrapper.le("create_date", endDate);
        }

        switch (sysUser.getRole()) {
            case ADMIN:
                break;
            default:
                wrapper.eq("user_id", sysUser.getId());
                break;
        }

        return userFeedbackService.pageUserFeedbackVO(page, wrapper);
    }

    /**
     * 获取配送员需要处理反馈列表
     * 所有无人处理，或者处理人是当前用户，且创建人非当前用户的订单反馈
     */
    @GetMapping("/handle-list")
    @PreAuthorize("hasRole('ROLE_COURIER')")
    public BootstrapTableVO<UserFeedbackVO> listHandleOrder(@RequestParam(required = false, defaultValue = "1") Integer current,
                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                            String status, String id,
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                            @AuthenticationPrincipal SysUser sysUser) {
        Page<UserFeedback> page = new Page<>(current, size);
        OrderItem orderItem = new OrderItem();
        orderItem.setAsc(false);
        orderItem.setColumn("create_date");
        List<OrderItem> orders = new ArrayList<>();
        orders.add(orderItem);
        page.setOrders(orders);
        QueryWrapper<UserFeedback> wrapper = new QueryWrapper<>();

        Integer feedStatus = StringUtils.toInteger(status, -1);
        if(FeedbackStatusEnum.getByStatus(feedStatus) != null) {
            wrapper.eq("status", feedStatus);
        }
        if(StringUtils.isNotBlank(id)) {
            wrapper.eq("id", id);
        }
        if(startDate != null) {
            wrapper.ge("create_date", startDate);
        }
        if(endDate != null) {
            wrapper.le("create_date", endDate);
        }

        wrapper.eq("type", FeedbackTypeEnum.ORDER.getType());
        wrapper.eq("handler", sysUser.getId()).or().isNull("handler");
        wrapper.ne("user_id", sysUser.getId());

        return userFeedbackService.pageUserFeedbackVO(page, wrapper);
    }

    /**
     * 创建反馈记录
     * - 仅支持 配送员 & 普通用户
     */
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_COURIER')")
    public ResponseResult createFeedback(String type, String orderId, String content, @AuthenticationPrincipal SysUser sysUser) {
        // 校验参数
        Integer feedbackType = StringUtils.toInteger(type, -1);
        FeedbackTypeEnum feedbackTypeEnum = FeedbackTypeEnum.getByType(feedbackType);
        if(feedbackTypeEnum == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.FEEDBACK_TYPE_ERROR);
        }

        if(StringUtils.isBlank(content)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.FEEDBACK_NOT_EMPTY);
        }
        if(content.length() > 255) {
            return ResponseResult.failure(ResponseErrorCodeEnum.STR_LENGTH_OVER, new Object[]{"反馈结果", 255});
        }
        if(StringUtils.isNotBlank(orderId)) {
            if(!orderInfoService.isExist(orderId)) {
                return ResponseResult.failure(ResponseErrorCodeEnum.ORDER_NOT_EXIST);
            }
        }

        boolean isSuccess = userFeedbackService.createFeedback(sysUser.getId(), feedbackTypeEnum, content, orderId);

        return isSuccess ? ResponseResult.success() : ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
    }

    /**
     * 批量撤销反馈，仅能撤销个人反馈
     * 状态为等待处理
     */
    @PostMapping("/batch-cancel")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_COURIER')")
    public ResponseResult batchCancel(String[] ids, @AuthenticationPrincipal SysUser sysUser) {
        return userFeedbackService.batchCancelOrder(ids, sysUser.getId());
    }

    /**
     * 审批反馈记录
     */
    @PostMapping("/deal")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_COURIER')")
    public ResponseResult dealFeedback(String id, Integer status, String result, @AuthenticationPrincipal SysUser sysUser) {
        // 校验参数
        UserFeedback feedback = userFeedbackService.getById(id);
        FeedbackStatusEnum statusEnum = FeedbackStatusEnum.getByStatus(status);
        if(feedback == null || statusEnum == null || StringUtils.isBlank(result)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        if(result.length() > 255) {
            return ResponseResult.failure(ResponseErrorCodeEnum.STR_LENGTH_OVER, new Object[]{"反馈结果", 255});
        }

        feedback.setHandler(sysUser.getId());
        feedback.setFeedbackStatus(statusEnum);
        feedback.setResult(result);

        boolean isSuccess = userFeedbackService.updateById(feedback);
        return isSuccess ? ResponseResult.success() : ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
    }
}
