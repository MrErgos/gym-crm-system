package io.github.mrergos.gymcrm.storage;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class StorageInitializationBeanPostProcessor implements BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(StorageInitializationBeanPostProcessor.class);
    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof StorageInitializer storageInitializer) {
            log.debug("BeanPostProcessor detected StorageInitializer bean '{}', triggering storage load", beanName);
            storageInitializer.loadStorage();
        }
        return bean;
    }
}
