package com.express.controller.api;

import com.express.common.constant.SessionKeyConstant;
import com.express.common.util.StringUtils;
import com.express.domain.ResponseResult;
import com.express.domain.bean.SysUser;
import com.express.domain.enums.ResponseErrorCodeEnum;
import com.express.domain.enums.SexEnum;
import com.express.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * API 设置信息
 */
@RestController
@RequestMapping("/api/v1/setting")
public class SettingApiController {
    @Autowired
    private SysUserService sysUserService;

    /**
     * 校验密码
     */
    @PostMapping("/check-password")
    public ResponseResult checkPassword(String password, @AuthenticationPrincipal SysUser sysUser) {
        if(StringUtils.isBlank(password)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        boolean isMatch = sysUserService.checkPassword(sysUser.getId(), password);
        return isMatch ? ResponseResult.success() : ResponseResult.failure(ResponseErrorCodeEnum.PASSWORD_ERROR);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public ResponseResult resetPassword(String origin, String target, @AuthenticationPrincipal SysUser sysUser) {
        if(StringUtils.isAnyBlank(origin, target)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        return sysUserService.resetPassword(sysUser.getId(), origin, target);
    }

    /**
     * 初始设置用户名、密码
     */
    @PostMapping("/username")
    public ResponseResult setUsernameAndPassword(String username, String password, @AuthenticationPrincipal SysUser sysUser) {
        if(StringUtils.isAnyBlank(username, password)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        return sysUserService.setUsernameAndPassword(sysUser, username, password);
    }

    /**
     * 初始设置实名信息
     */
    @PostMapping("/real-name")
    public ResponseResult setRealName(String realName, String idCard, @AuthenticationPrincipal SysUser sysUser) {
        if(StringUtils.isAnyBlank(realName, idCard)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        return sysUserService.setRealName(sysUser, realName, idCard);
    }

    /**
     * 设置/修改手机号
     */
    @PostMapping("/tel")
    public ResponseResult setTel(String tel, String code, HttpSession session, @AuthenticationPrincipal SysUser sysUser) {
        if(StringUtils.isAnyBlank(tel, code)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        return sysUserService.setTel(sysUser, tel, code, session);
    }

    /**
     * 设置/修改性别
     */
    @PostMapping("/sex")
    public ResponseResult setSex(Integer sex, @AuthenticationPrincipal SysUser sysUser) {
        SexEnum sexEnum = SexEnum.getByType(sex);
        if(sexEnum == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        return sysUserService.setSex(sysUser, sexEnum);
    }

    /**
     * 设置修改高校信息
     */
    @PostMapping("/school")
    public ResponseResult setSchool(Integer school, String studentIdCard, @AuthenticationPrincipal SysUser sysUser) {
        if(StringUtils.isBlank(studentIdCard) || school == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        return sysUserService.setSchoolInfo(sysUser, school, studentIdCard);
    }

    /**
     * 设置或更新人脸数据
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/face")
    public ResponseResult setOrUpdateFace(HttpSession session, @AuthenticationPrincipal SysUser sysUser) {
        try {
            String faceToken = (String) session.getAttribute(SessionKeyConstant.LOGIN_FACE_TOKEN);
            return sysUserService.bindOrUpdateFace(faceToken, sysUser.getId());
        } catch (Exception e) {
            return ResponseResult.failure(ResponseErrorCodeEnum.RETRY_ERROR);
        }
    }


    /**
     * 申请角色
     * 仅支持 user <-> courier
     */
    @PostMapping("/apply-role")
    public ResponseResult changeRole(String password, @AuthenticationPrincipal SysUser sysUser) {
        if(!sysUserService.checkPassword(sysUser.getId(), password)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PASSWORD_ERROR);
        }

        return sysUserService.changeRole(sysUser.getId());
    }
}
