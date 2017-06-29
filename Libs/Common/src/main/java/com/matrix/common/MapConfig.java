package com.matrix.common;

import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * A convenient type safe wrapper for application configurations in a {@link Map}.
 */
public class MapConfig extends AbstractConfig {

    private final Map<String, Object> config;

    public MapConfig(Map<String, Object> config) {
        Preconditions.checkArgument(config != null, "config is null");
        this.config = config;
    }

    @Override
    public Object get(String key) {
        return config.get(key);
    }
}
