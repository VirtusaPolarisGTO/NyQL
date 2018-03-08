package com.virtusa.gto.nyql.utils

import groovy.transform.CompileStatic
/**
 * @author iweerarathna
 */
@CompileStatic
class ReflectUtils {

    static <T> List<T> findServices(Class<T> clz, ClassLoader classLoader = null) {
        def load = ServiceLoader.load(clz, classLoader)
        def iterator = load.iterator()

        List<T> list = []
        while (iterator.hasNext()) {
            list.add(iterator.next())
        }
        return list
    }

    static <T> T callStaticMethod(String clzName, ClassLoader classLoader, Object... args) {
        def clazz = classLoader.loadClass(clzName)
        def method = clazz.getMethod('createNew', Map)
        (T) method.invoke(null, args)
    }

    static <T> T newInstance(String clzName, ClassLoader classLoader, Object... constructorArgs) {
        def clazz = classLoader.loadClass(clzName)
        (T) clazz.newInstance(constructorArgs)
    }
}
