package com.nequi.franchise.model.commons;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NameRules {

    public static final int MAX_LENGTH = 100;

    public static boolean isValid(String name) {
        return name != null && !name.isBlank() && name.strip().length() <= MAX_LENGTH;
    }
}
