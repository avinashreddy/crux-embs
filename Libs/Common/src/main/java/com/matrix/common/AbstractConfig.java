package com.matrix.common;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import rapture.common.CallingContext;
import rapture.common.api.DecisionApi;
import rapture.kernel.Kernel;

public abstract class AbstractConfig implements Config {

    @Override
    public String getString(String key) {
        return get(key, String.class);
    }

    @Override
    public String getString(String key, String defaultVal) {
        return get(key, String.class, defaultVal);
    }

    @Override
    public Integer getInt(String key) {
        return get(key, Integer.class);
    }

    @Override
    public Integer getInt(String key, Integer defaultVal) {
        return get(key, Integer.class, defaultVal);
    }


    private <T> T get(String key, Class<T> expectedType) {
        return get(key, expectedType, null, false);
    }

    @Override
    public <T> T get(String key, Class<T> expectedType, T defaultVal) {
        return get(key, expectedType, defaultVal, true);
    }

    protected  <T> T get(String key, Class<T> expectedType, T defaultVal, boolean hasDefault) {
        Preconditions.checkArgument(key != null, "key is null");
        Object val = get(key);
        if (val == null && hasDefault) {
            return defaultVal;
        }
        Preconditions.checkState(val != null, "null value for key '%s'", key);

        Preconditions.checkState(val.getClass().isAssignableFrom(expectedType), "Invalid value %s for key '%s'." +
                " Expected %s. Found %s", val, key, expectedType.getName(), val.getClass().getName());

        return (T) val;
    }
}
