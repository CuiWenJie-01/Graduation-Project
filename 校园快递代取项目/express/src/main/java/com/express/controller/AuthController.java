package com.express.controller;

import com.express.common.constant.SecurityConstant;
import com.express.common.constant.SessionKeyConstant;
import com.express.common.util.IDValidateUtils;
import com.express.domain.ResponseResult;
import com.express.domain.bean.SysUser;
import com.express.domain.enums.ResponseErrorCodeEnum;
import com.express.domain.enums.SexEnum;
import com.express.domain.enums.SysRoleEnum;
import com.express.security.validate.third.ThirdLoginAuthenticationToken;
import com.express.common.util.StringUtils;
import com.express.service.AipService;
import com.express.service.DataSchoolService;
import com.express.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
public class AuthController {
    @Autowired
    private AipService aipService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private DataSchoolService dataSchoolService;
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 验证图形验证码
     */
    @PostMapping(SecurityConstant.VALIDATE_CODE_URL_PREFIX + "/check-img")
    public ResponseResult checkVerifyCode(String code, HttpSession session) {
        if(StringUtils.isBlank(code)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        Object systemCode = session.getAttribute("validateCode");
        if(systemCode == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.SYSTEM_ERROR);
        }

        String validateCode = ((String)systemCode).toLowerCase();

        if(!validateCode.equals(code.toLowerCase())) {
            return ResponseResult.failure(ResponseErrorCodeEnum.VERIFY_CODE_ERROR);
        }

        return ResponseResult.success();
    }


    /**
     * 人脸登录
     */
    @SuppressWarnings("Duplicates")
    @PostMapping("/auth/face-login")
    public ResponseResult faceLogin(String data) {
        String base64Prefix = "data:image/png;base64,";
        if(StringUtils.isBlank(data)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        if(data.startsWith(base64Prefix)) {
            data = data.substring(base64Prefix.length());
        }

        ResponseResult result = aipService.faceSearchByBase64(data);
        if(result.getCode() != ResponseErrorCodeEnum.SUCCESS.getCode()) {
            return result;
        }

        // 人脸登录和三方登录一样，无需鉴权，因此使用三方登录的方式注入框架即可
        SysUser sysUser = (SysUser) result.getData();
        ThirdLoginAuthenticationToken token = new ThirdLoginAuthenticationToken(sysUser.getId());
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ResponseResult.success();
    }

    /**
     * 人脸校验
     * 未登录：存session
     * 已登录：存redis
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/auth/face-check")
    public ResponseResult faceCheck(HttpSession session, String data, @AuthenticationPrincipal SysUser sysUser) {
        String base64Prefix = "data:image/png;base64,";
        if(StringUtils.isBlank(data)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        if(data.startsWith(base64Prefix)) {
            data = data.substring(base64Prefix.length());
        }
        ResponseResult result = aipService.faceDetectByBase64(data, true);
        if(result.getCode() != ResponseErrorCodeEnum.SUCCESS.getCode()) {
            return result;
        }

        if (sysUser == null) {
            // 暂存face_token于session，用于人脸注册
            session.setAttribute(SessionKeyConstant.REGISTER_FACE_TOKEN, result.getData());
        } else {
            try {
                Map<String, String> resultData = (Map<String, String>) result.getData();
                String faceToken = resultData.get("face_token");
                session.setAttribute(SessionKeyConstant.LOGIN_FACE_TOKEN, faceToken);
            } catch (Exception e) {
                return ResponseResult.failure(ResponseErrorCodeEnum.RETRY_ERROR);
            }
        }

        return ResponseResult.success();
    }

    /**
     * 用户注册
     * @param type 注册类型 1：用户名密码；3：人脸
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/auth/register")
    public ResponseResult register(Integer type, String username, String password,
                                   String tel, String code, HttpSession session) {
        if(type == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        switch (type) {
            case 1:
                if(StringUtils.isAnyBlank(username, password)) {
                    return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
                }
                return sysUserService.registryByUsername(username, password);
            case 3:
                // 读取face_token
                Map<String, String> map = (Map<String, String>) session.getAttribute(SessionKeyConstant.REGISTER_FACE_TOKEN);
                String faceToken = map.get("face_token");
                String gender = map.get("gender");
                if(StringUtils.isAnyBlank(faceToken, gender)) {
                    return ResponseResult.failure(ResponseErrorCodeEnum.NOT_FACE_TO_REGISTRY);
                }

                return sysUserService.registryByFace(faceToken, gender);
            default:
                return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
    }

    /**
     * 信息补全
     */
    @PostMapping("/auth/complete-info")
    public ResponseResult completeInfo(Integer role, Integer school, Integer sex, String studentIdCard, String realName, String idCard,
                                       @AuthenticationPrincipal SysUser sysUser) {
        if(role == null || school == null || sex == null || StringUtils.isBlank(studentIdCard)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }

        // 仅支持申请普通用户、配送员
        SysRoleEnum roleEnum = SysRoleEnum.getByType(role);
        if(roleEnum != SysRoleEnum.USER && roleEnum != SysRoleEnum.COURIER) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        sysUser.setRole(roleEnum);

        // 校验 schoolId
        if(!dataSchoolService.isExist(school)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.SCHOOL_NOT_EXIST);
        }
        // 校验 studentIdCard
        if(!StringUtils.isNumeric(studentIdCard)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.STUDENT_IDCARD_NOT_NUMBER);
        }

        sysUser.setSchoolId(school);
        sysUser.setStudentIdCard(studentIdCard);

        // 校验性别
        SexEnum sexEnum = SexEnum.getByType(sex);
        if(sexEnum == null) {
            return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
        }
        sysUser.setSex(sexEnum);

        // 配送员必填真实姓名、身份证号
        if(roleEnum == SysRoleEnum.COURIER) {
            if(StringUtils.isAnyBlank(realName, idCard)) {
                return ResponseResult.failure(ResponseErrorCodeEnum.PARAMETER_ERROR);
            }
            if(!IDValidateUtils.check(idCard)) {
                return ResponseResult.failure(ResponseErrorCodeEnum.ID_CARD_INVALID);
            }
            if(StringUtils.containsSpecial(realName) || StringUtils.containsNumber(realName)) {
                return ResponseResult.failure(ResponseErrorCodeEnum.REAL_NAME_INVALID);
            }

            sysUser.setIdCard(idCard);
            sysUser.setRealName(realName);
        }

        if(!sysUserService.updateById(sysUser)) {
            return ResponseResult.failure(ResponseErrorCodeEnum.OPERATION_ERROR);
        }

        return ResponseResult.success();
    }
}
