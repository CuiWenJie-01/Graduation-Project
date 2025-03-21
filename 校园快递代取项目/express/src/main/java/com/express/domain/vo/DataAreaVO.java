package com.express.domain.vo;

import com.express.common.util.CollectionUtils;
import com.express.domain.bean.DataArea;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DataAreaVO implements Serializable {
    /**
     * ID
     */
    private Integer id;
    /**
     * 名称
     */
    private String name;

    public static List<DataAreaVO> convert(List<DataArea> areas) {
        if(CollectionUtils.isListEmpty(areas)) {
            return Collections.emptyList();
        }

        return areas.stream().map(DataAreaVO::convert).collect(Collectors.toList());
    }

    public static DataAreaVO convert(DataArea area) {
        DataAreaVO vo = new DataAreaVO();
        BeanUtils.copyProperties(area, vo);

        return vo;
    }
}
