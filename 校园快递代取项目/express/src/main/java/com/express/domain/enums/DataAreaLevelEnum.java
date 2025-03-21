package com.express.domain.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum DataAreaLevelEnum implements IEnum<Integer> {
    PROVINCE(1, "省/直辖市"),
    CITY(2, "地级市"),
    COUNTY(3, "区县"),
    TOWN(4, "镇/街");

    private int level;

    private String name;

    DataAreaLevelEnum(int level, String name) {
        this.level = level;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return this.level;
    }
}
