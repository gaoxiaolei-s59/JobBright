package org.puregxl.site.jobbacked.common.enums;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * 职位等级枚举类
 */
public enum ExperienceLevelEnum {

    /**
     * 在校生
     */
    STUDENT("STUDENT", "在校生", new String[]{"在校生", "学生", "实习生"}),

    /**
     * 应届生
     */
    NEW_GRAD("NEW_GRAD", "应届生", new String[]{"应届生", "校招生", "毕业生"}),

    /**
     * 初级岗位
     */
    JUNIOR("JUNIOR", "初级岗位", new String[]{"初级岗位", "初级", "1-3年"});

    private final String code;

    private final String description;

    private final String[] aliases;

    ExperienceLevelEnum(String code, String description, String[] aliases) {
        this.code = code;
        this.description = description;
        this.aliases = aliases;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static String normalize(String value) {
        return fromValue(value)
                .map(ExperienceLevelEnum::getCode)
                .orElse(value);
    }

    public static String getDescriptionByCode(String code) {
        return fromValue(code)
                .map(ExperienceLevelEnum::getDescription)
                .orElse(code);
    }

    private static Optional<ExperienceLevelEnum> fromValue(String value) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(value)
                        || item.description.equals(value)
                        || Arrays.stream(item.aliases).anyMatch(alias -> alias.equalsIgnoreCase(value)))
                .findFirst();
    }
}
