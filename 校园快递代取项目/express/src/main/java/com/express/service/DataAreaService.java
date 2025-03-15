package com.express.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.express.domain.bean.DataArea;
import com.express.domain.vo.DataAreaVO;

import java.util.List;

public interface DataAreaService extends IService<DataArea> {
    List<DataArea> listByParentId(Integer parentId);

    List<DataAreaVO> listByParentIdByCache(Integer parentId);
}
