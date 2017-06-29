package com.matrix.common;

import java.util.Map;

/**
 * A convenient type safe wrapper for application configurations.
 */
public interface Config {

    Object get(String key);

    String getString(String key);

    String getString(String key, String defaultVal);

    Integer getInt(String key);

    Integer getInt(String key, Integer defaultVal);

    <T> T get(String key, Class<T> expectedType, T defaultVal);
}
