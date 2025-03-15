package com.express.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.express.domain.bean.SysUser;

public interface SysUserMapper extends BaseMapper<SysUser> {
    SysUser selectByIdCard(String idCard);
}
