package com.express.domain.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.express.domain.enums.DataAreaLevelEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.io.Serializable;

/**
 * 行政区域数据
 */
@Data
public class DataArea implements Serializable {
    @TableId
    private Integer id;
    /**
     * 父ID
     */
    private Integer parentId;
    /**
     * 名称
     */
    private String name;
    /**
     * 简称
     */
    private String shortName;
    /**
     * 经度
     */
    private Float longitude;
    /**
     * 纬度
     */
    private Float latitude;
    /**
     * 等级(1省/直辖市,2地级市,3区县,4镇/街道)
     */
    @JsonValue
    private DataAreaLevelEnum level;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 状态（0：启用；1：禁用）
     */
    @TableLogic
    private Integer status;
}

