package com.express.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.express.common.util.DoubleUtils;
import com.express.common.util.StringUtils;
import com.express.domain.ResponseResult;
import com.express.domain.bean.OrderEvaluate;
import com.express.domain.bean.SysUser;
import com.express.domain.enums.ResponseErrorCodeEnum;
import com.express.service.OrderEvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 评分接口
 */
@RestController
@RequestMapping("/api/v1/evaluate")
public class EvaluateApiController {
    @Autowired
    private OrderEvaluateService orderEvaluateService;

    /**
     * 订单评分
     */
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_COURIER')")
    public ResponseResult orderEvaluate(@PathVariable String orderId, String score,
                                        String evaluate, @AuthenticationPrincipal SysUser sysUser) {
        double evaluateScore = getScore(score);
        if(evaluateScore == -1) {
            return ResponseResult.failure(ResponseErrorCodeEnum.EVALUATE_SCORE_ERROR);
        }

        switch (sysUser.getRole()) {
            case USER:
                return orderEvaluateService.userEvaluate(orderId, sysUser.getId(), evaluateScore, evaluate);
            case COURIER:
                return orderEvaluateService.courierEvaluate(orderId, sysUser.getId(), evaluateScore, evaluate);
            default:
                return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
        }
    }

    /**
     * 分页个人评价列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_COURIER')")
    public ResponseResult listEvaluate(@RequestParam(required = false, defaultValue = "1") Integer current,
                                       @AuthenticationPrincipal SysUser sysUser) {
        if(current < 1) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        Page<OrderEvaluate> page = new Page<>(current, 10);
        String userId = sysUser.getId();

        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> record = new ArrayList<>();

        IPage<OrderEvaluate> selectPage;
                switch (sysUser.getRole()) {
            case USER:
                selectPage = orderEvaluateService.page(page, new QueryWrapper<OrderEvaluate>().eq("user_id", userId));
                for(OrderEvaluate evaluate : selectPage.getRecords()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("orderId", evaluate.getId());
                    map.put("score", evaluate.getCourierScore().toPlainString());
                    map.put("evaluate", evaluate.getCourierEvaluate());
                    record.add(map);
                }
                break;
            case COURIER:
                selectPage = orderEvaluateService.page(page, new QueryWrapper<OrderEvaluate>().eq("courier_id", userId));
                for(OrderEvaluate evaluate : selectPage.getRecords()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("orderId", evaluate.getId());
                    if(evaluate.getUserScore()!=null){
                        map.put("score", evaluate.getUserScore().toPlainString());
                        map.put("evaluate", evaluate.getUserEvaluate());
                    } else {
                        map.put("score", "用户还未评价");
                        map.put("evaluate", "用户还未评价");
                    }
                    record.add(map);
                }
                break;
            default:
                return ResponseResult.failure(ResponseErrorCodeEnum.NO_PERMISSION);
        }

        result.put("record", record);
        result.put("current", current);
        result.put("page", selectPage.getPages());
        return ResponseResult.success(result);
    }

    /**
     * 获取评分，取值0~10，三位小数
     */
    private double getScore(String score) {
        Double result = StringUtils.toDouble(score, -1D);
        return DoubleUtils.round(result, 3);
    }
}
