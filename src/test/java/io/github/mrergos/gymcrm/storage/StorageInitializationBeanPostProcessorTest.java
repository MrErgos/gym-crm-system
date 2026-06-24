package io.github.mrergos.gymcrm.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageInitializationBeanPostProcessor tests")
class StorageInitializationBeanPostProcessorTest {

    private final StorageInitializationBeanPostProcessor processor =
            new StorageInitializationBeanPostProcessor();

    @Test
    @DisplayName("postProcessAfterInitialization: triggers loadStorage for StorageInitializer bean")
    void postProcess_storageInitializer_shouldTriggerLoad() {
        //given
        StorageInitializer initializer = mock(StorageInitializer.class);

        //when
        Object result = processor.postProcessAfterInitialization(initializer, "storageInitializer");

        //then
        verify(initializer).loadStorage();
        assertSame(initializer, result);
    }

    @Test
    @DisplayName("postProcessAfterInitialization: ignores non-StorageInitializer beans")
    void postProcess_otherBean_shouldNotTriggerLoad() {
        //given
        Object someBean = new Object();

        //when
        Object result = processor.postProcessAfterInitialization(someBean, "someBean");

        //then
        assertSame(someBean, result);
    }
}