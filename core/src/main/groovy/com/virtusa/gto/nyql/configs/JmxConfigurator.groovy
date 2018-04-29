package com.virtusa.gto.nyql.configs

import com.virtusa.gto.nyql.exceptions.NyConfigurationException
import com.virtusa.gto.nyql.exceptions.NyException
import com.virtusa.gto.nyql.model.NyQLInstanceMXBean
import com.virtusa.gto.nyql.utils.Constants
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.management.MBeanServer
import javax.management.ObjectName
import java.lang.management.ManagementFactory
/**
 * @author iweerarathna
 */
@CompileStatic
class JmxConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxConfigurator)

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer()

    @PackageScope
    JmxConfigurator() {}

    void registerMXBean(NyQLInstanceMXBean nyQLMXBean) throws NyConfigurationException {
        ObjectName name = getName(nyQLMXBean)

        try {
            if (mBeanServer.isRegistered(name)) {
                mBeanServer.unregisterMBean(name);
            }
            mBeanServer.registerMBean(nyQLMXBean, name)
            LOGGER.info("Successfully registered MXBean: " + name.getCanonicalName())
        } catch (Exception ex) {
            throw new NyConfigurationException("Failed to configure MXBean '${name.getCanonicalName()}' to platform server!", ex)
        }
    }

    void removeMXBean(NyQLInstanceMXBean nyQLMXBean) {
        ObjectName name = getName(nyQLMXBean)

        try {
            mBeanServer.unregisterMBean(name)
        } catch (Exception ex) {
            throw new NyException("Failed to remove MXBean: '${name.getCanonicalName()}' from platform server!", ex)
        }
    }

    private static ObjectName getName(NyQLInstanceMXBean nyQLMXBean) {
        return new ObjectName(Constants.MBEAN_OBJECT_NAME + ":type=NyQLInstance,name=" + nyQLMXBean.getName())
    }

    static JmxConfigurator get() {
        return Holder.INSTANCE
    }

    private static class Holder {
        private static final JmxConfigurator INSTANCE = new JmxConfigurator()
    }
}
