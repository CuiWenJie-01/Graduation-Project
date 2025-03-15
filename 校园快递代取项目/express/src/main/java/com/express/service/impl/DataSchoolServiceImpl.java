package com.express.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.express.domain.bean.DataSchool;
import com.express.mapper.DataSchoolMapper;
import com.express.service.DataSchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public class DataSchoolServiceImpl extends ServiceImpl<DataSchoolMapper, DataSchool> implements DataSchoolService {
    @Autowired
    private DataSchoolMapper dataSchoolMapper;

    @Override
    public DataSchool getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    public boolean updateById(DataSchool entity) {
        return super.updateById(entity);
    }

    @Override
    public boolean isExist(Integer id) {
        return dataSchoolMapper.selectById(id) != null;
    }

    @Override
    public List<DataSchool> listByProvinceId(Integer provinceId) {
        return dataSchoolMapper.selectList(new QueryWrapper<DataSchool>().eq("province_id", provinceId));
    }

    @Override
    public List<DataSchool> listByProvinceIdByCache(Integer provinceId) {
        return listByProvinceId(provinceId);
    }
}
