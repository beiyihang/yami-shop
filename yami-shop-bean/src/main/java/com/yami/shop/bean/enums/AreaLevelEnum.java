
package com.yami.shop.bean.enums;

/**
 * 地区层级
 * @author cl
 */
public enum AreaLevelEnum {


    /**
     * 第一层
     */
    FIRST_LEVEL(1),

    /**
     * 第二层
     */
    SECOND_LEVEL(2),

    /**
     * 第三层
     */
    THIRD_LEVEL(3)

    ;

    private Integer num;

    public Integer value() {
        return num;
    }

    AreaLevelEnum(Integer num) {
        this.num = num;
    }

    public static AreaLevelEnum instance(Integer value) {
        // 获取所有的AreaLevelEnum枚举值
        AreaLevelEnum[] enums = values();

        // 遍历枚举值数组
        for (AreaLevelEnum statusEnum : enums) {
            // 检查枚举值的value属性是否与给定的value相等
            if (statusEnum.value().equals(value)) {
                // 如果相等，返回该枚举值
                return statusEnum;
            }
        }

        // 如果没有匹配的枚举值，返回null
        return null;
    }

}
