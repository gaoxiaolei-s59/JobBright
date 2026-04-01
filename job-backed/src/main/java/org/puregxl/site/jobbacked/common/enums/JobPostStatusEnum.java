package org.puregxl.site.jobbacked.common.enums;


/**
 * 职位状态枚举类
 */
public enum JobPostStatusEnum {

    /**
     * 在线
     */
    ONLINE("ONLINE", "在线"),

    /**
     * 下线
     */
    OFFLINE("OFFLINE", "下线"),

    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 已过期
     */
    EXPIRED("EXPIRED", "已过期");

    private final String code;

    private final String description;

    JobPostStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
