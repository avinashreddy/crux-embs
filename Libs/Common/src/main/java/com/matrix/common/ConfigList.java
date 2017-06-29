package com.matrix.common;

public class ConfigList extends AbstractConfig {

    private Config[] configs;

    public ConfigList(Config... configs) {
        this.configs = configs;
    }

    @Override
    public Object get(String key) {
        for(Config c : configs) {
            Object val = c.get(key);
            if(val != null) {
                return val;
            }
        }
        return null;
    }
}
