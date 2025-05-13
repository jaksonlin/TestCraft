package com.github.jaksonlin.testcraft.domain.annotations;

public enum ValueProviderType {
    GIT_AUTHOR,
    FIRST_CREATOR_AUTHOR,
    FIRST_CREATOR_TIME,
    LAST_MODIFIER_AUTHOR,
    LAST_MODIFIER_TIME,
    CURRENT_DATE,
    METHOD_NAME_BASED,
    FIXED_STRING_VALUE,
    FIXED_STRING_LIST,
    CLASS_NAME,
    METHOD_NAME,
    METHOD_SIGNATURE
}
