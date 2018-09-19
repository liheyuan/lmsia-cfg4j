package com.coder4.lmsia.cfg4j;

import org.cfg4j.provider.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

/**
 * @author coder4
 */
@Service
@ConditionalOnBean(ConfigurationProvider.class)
public class Cfg4jValueProcessor implements BeanPostProcessor, Ordered {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConfigurationProvider configurationProvider;

    // 初始化前注入
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        final Class targetClass = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithFields(targetClass, field -> process(bean, targetClass, field), field -> {
            return field.isAnnotationPresent(Cfg4jValue.class);
        });
        return bean;
    }

    private void process(final Object bean, Class<?> targetClass, final Field field) {
        // Get injected field name
        Cfg4jValue valueAnnotation = field.getDeclaredAnnotation(Cfg4jValue.class);
        String fieldName = getPropName(valueAnnotation, field.getName());
        // inject for some support type
        fieldSetWithSupport(bean, field, fieldName);
    }

    private void fieldSetWithSupport(Object bean, Field field, String key) {
        Class type = field.getType();
        field.setAccessible(true);
        try {
            if (int.class == type || Integer.class == type) {
                field.set(bean, configurationProvider.getProperty(key, Integer.class));
            } else if (boolean.class == type || Boolean.class == type) {
                field.set(bean, configurationProvider.getProperty(key, Boolean.class));
            } else if (String.class == type) {
                field.set(bean, configurationProvider.getProperty(key, String.class));
            } else if (long.class == type || Long.class == type) {
                field.set(bean, configurationProvider.getProperty(key, Long.class));
            } else {
                LOG.error("not support cfj4j value inject type");
                throw new RuntimeException("not supported cfg4jValue type");
            }
        } catch (IllegalAccessException e) {
            LOG.error("exception during field set", e);
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            LOG.error("config missing key, please check");
            throw new RuntimeException(e);
        }
    }

    public static String getPropName(Cfg4jValue annotation, String defaultName) {
        String key = annotation.value();
        if (key == null || key.isEmpty()) {
            key = defaultName;
        }
        return key;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws
            BeansException {
        return bean;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}