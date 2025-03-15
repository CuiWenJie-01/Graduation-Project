package com.express.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.express.domain.bean.DataCompany;
import com.express.mapper.DataCompanyMapper;
import com.express.service.DataCompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DataCompanyServiceImpl extends ServiceImpl<DataCompanyMapper, DataCompany> implements DataCompanyService {
    @Autowired
    private DataCompanyMapper dataCompanyMapper;

    @Override
    public List<DataCompany> listAll() {
        return dataCompanyMapper.selectList(null);
    }

    @Override
    public List<DataCompany> listAllByCache() {
        return listAll();
    }

    @Override
    public DataCompany getByCache(Integer id) {
        return getById(id);
    }

}
