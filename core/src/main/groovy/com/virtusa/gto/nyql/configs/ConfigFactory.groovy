package com.virtusa.gto.nyql.configs

import groovy.transform.PackageScope

/**
 * @author iweerarathna
 */
@PackageScope
class ConfigFactory {

    private ConfigFactory() {}

    static Configurations create(Map configs) {
        Integer version = (Integer) configs.get('version');
        if (version != null && version == 2) {
            return new ConfigurationsV2()
        } else {
            return new Configurations()
        }
    }

}
