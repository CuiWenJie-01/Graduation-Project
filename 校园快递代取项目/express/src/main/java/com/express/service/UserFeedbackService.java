package com.express.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.express.domain.ResponseResult;
import com.express.domain.bean.UserFeedback;
import com.express.domain.enums.FeedbackTypeEnum;
import com.express.domain.vo.BootstrapTableVO;
import com.express.domain.vo.UserFeedbackVO;

import java.util.Map;


public interface UserFeedbackService extends IService<UserFeedback> {
    /**
     * 分页查询前台反馈信息
     */
    BootstrapTableVO<UserFeedbackVO> pageUserFeedbackVO(Page<UserFeedback> page, QueryWrapper<UserFeedback> wrapper);

    boolean createFeedback(String userId, FeedbackTypeEnum feedbackTypeEnum, String content, String orderId);

    ResponseResult batchCancelOrder(String[] ids, String id);

    Map<String, Long> getAdminDashboardData();

    Map<String, Long> getUserDashboardData(String userId);

    Map<String, Long> getCourierDashboardData();
}
