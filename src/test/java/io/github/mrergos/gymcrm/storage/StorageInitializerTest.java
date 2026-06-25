package io.github.mrergos.gymcrm.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageInitializer tests")
class StorageInitializerTest {

    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Resource resource;

    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;
    private Map<Long, Training> trainingStorage;

    private StorageInitializer initializer;

    @BeforeEach
    void setUp() {
        traineeStorage = new HashMap<>();
        trainerStorage = new HashMap<>();
        trainingStorage = new HashMap<>();

        initializer = new StorageInitializer();
        initializer.setTraineeStorage(traineeStorage);
        initializer.setTrainerStorage(trainerStorage);
        initializer.setTrainingStorage(trainingStorage);
        initializer.setResourceLoader(resourceLoader);
        initializer.setObjectMapper(objectMapper);
    }

    private void setFilePath(String path) {
        ReflectionTestUtils.setField(initializer, "filePath", path);
    }

    @Test
    @DisplayName("loadStorage: blank filepath leaves storage empty")
    void loadStorage_blankPath_shouldDoNothing() {
        //given
        setFilePath("  ");

        //when
        initializer.loadStorage();

        //then
        assertTrue(traineeStorage.isEmpty());
        verifyNoInteractions(resourceLoader, objectMapper);
    }

    @Test
    @DisplayName("loadStorage: null filepath leaves storage empty")
    void loadStorage_nullPath_shouldDoNothing() {
        //given
        setFilePath(null);

        //when
        initializer.loadStorage();

        //then
        assertTrue(traineeStorage.isEmpty());
        verifyNoInteractions(resourceLoader, objectMapper);
    }

    @Test
    @DisplayName("loadStorage: non-existent file leaves storage empty")
    void loadStorage_fileNotExists_shouldDoNothing() {
        //given
        setFilePath("classpath:missing.json");
        when(resourceLoader.getResource("classpath:missing.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        //when
        initializer.loadStorage();

        //then
        assertTrue(traineeStorage.isEmpty());
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("loadStorage: path pointing to directory leaves storage empty")
    void loadStorage_directory_shouldDoNothing() throws Exception {
        //given
        setFilePath("classpath:someDir");
        when(resourceLoader.getResource("classpath:someDir")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(resource.isFile()).thenReturn(false);

        //when
        initializer.loadStorage();

        //then
        assertTrue(traineeStorage.isEmpty());
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("loadStorage: empty file leaves storage empty")
    void loadStorage_emptyFile_shouldDoNothing() throws Exception {
        //given
        setFilePath("classpath:data.json");
        when(resourceLoader.getResource("classpath:data.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(resource.isFile()).thenReturn(true);
        when(resource.contentLength()).thenReturn(0L);

        //when
        initializer.loadStorage();

        //then
        assertTrue(traineeStorage.isEmpty());
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("loadStorage: IOException is handled gracefully")
    void loadStorage_ioException_shouldNotThrow() throws Exception {
        //given
        setFilePath("classpath:data.json");
        when(resourceLoader.getResource("classpath:data.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenThrow(new IOException("boom"));

        //when
        // then
        assertDoesNotThrow(() -> initializer.loadStorage());
        assertTrue(traineeStorage.isEmpty());
    }

    @Test
    @DisplayName("loadStorage: valid file populates all storages")
    void loadStorage_validFile_shouldPopulateStorage() throws Exception {
        //given
        setFilePath("classpath:data.json");
        when(resourceLoader.getResource("classpath:data.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(resource.isFile()).thenReturn(true);
        when(resource.contentLength()).thenReturn(100L);

        Trainee trainee = new Trainee();
        trainee.setUserId(1L);
        Trainer trainer = new Trainer();
        trainer.setUserId(2L);
        Training training = new Training();
        training.setId(3L);

        StorageDTO dto = new StorageDTO();
        dto.setTrainees(List.of(trainee));
        dto.setTrainers(List.of(trainer));
        dto.setTrainings(List.of(training));

        when(objectMapper.readValue(any(InputStream.class), eq(StorageDTO.class)))
                .thenReturn(dto);

        //when
        initializer.loadStorage();

        //then
        assertEquals(1, traineeStorage.size());
        assertEquals(1, trainerStorage.size());
        assertEquals(1, trainingStorage.size());
        assertSame(trainee, traineeStorage.get(1L));
        assertSame(trainer, trainerStorage.get(2L));
        assertSame(training, trainingStorage.get(3L));
    }


    @Test
    @DisplayName("saveStorage: blank filepath skips save")
    void saveStorage_blankPath_shouldDoNothing() {
        //given
        setFilePath("  ");
        //when
        initializer.saveStorage();
        //then
        verifyNoInteractions(resourceLoader, objectMapper);
    }

    @Test
    @DisplayName("saveStorage: null filepath skips save")
    void saveStorage_nullPath_shouldDoNothing() {
        //given
        setFilePath(null);
        //when
        initializer.saveStorage();
        //then
        verifyNoInteractions(resourceLoader, objectMapper);
    }

    @Test
    @DisplayName("saveStorage: IOException is handled gracefully")
    void saveStorage_ioException_shouldNotThrow() throws Exception {
        //given
        setFilePath("file:/tmp/test_data.json");
        when(resourceLoader.getResource("file:/tmp/test_data.json")).thenReturn(resource);
        when(resource.getFile()).thenThrow(new IOException("disk full"));

        //when
        //then
        assertDoesNotThrow(() -> initializer.saveStorage());
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("saveStorage: parent dir is null - skips mkdirs, writes file")
    void saveStorage_nullParentDir_shouldSkipMkdirsAndWrite() throws Exception {
        //given
        setFilePath("file:/test_data.json");

        File mockFile = mock(File.class);

        when(resourceLoader.getResource("file:/test_data.json")).thenReturn(resource);
        when(resource.getFile()).thenReturn(mockFile);
        when(mockFile.getParentFile()).thenReturn(null);
        when(mockFile.exists()).thenReturn(true);

        ObjectWriter mockWriter = mock(ObjectWriter.class);
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);

        //when
        initializer.saveStorage();

        //then
        verify(mockWriter).writeValue(eq(mockFile), any(StorageDTO.class));
    }

    @Test
    @DisplayName("saveStorage: parent dir missing and mkdirs succeeds - writes file")
    void saveStorage_parentDirMissing_mkdirsSucceeds_shouldWriteFile() throws Exception {
        //given
        setFilePath("file:/new/path/test_data.json");

        File mockFile = mock(File.class);
        File mockParent = mock(File.class);

        when(resourceLoader.getResource("file:/new/path/test_data.json")).thenReturn(resource);
        when(resource.getFile()).thenReturn(mockFile);
        when(mockFile.getParentFile()).thenReturn(mockParent);
        when(mockParent.exists()).thenReturn(false);
        when(mockParent.mkdirs()).thenReturn(true);
        when(mockFile.exists()).thenReturn(true);

        ObjectWriter mockWriter = mock(ObjectWriter.class);
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);

        //when
        initializer.saveStorage();

        //then
        verify(mockParent).mkdirs();
        verify(mockWriter).writeValue(eq(mockFile), any(StorageDTO.class));
    }

    @Test
    @DisplayName("saveStorage: parent dir missing and mkdirs fails - skips save")
    void saveStorage_parentDirMissing_mkdirsFails_shouldDoNothing() throws Exception {
        //given
        setFilePath("file:/bad/path/test_data.json");

        File mockFile = mock(File.class);
        File mockParent = mock(File.class);

        when(resourceLoader.getResource("file:/bad/path/test_data.json")).thenReturn(resource);
        when(resource.getFile()).thenReturn(mockFile);
        when(mockFile.getParentFile()).thenReturn(mockParent);
        when(mockParent.exists()).thenReturn(false);
        when(mockParent.mkdirs()).thenReturn(false);

        //when
        initializer.saveStorage();

        //then
        verify(mockParent).mkdirs();
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("saveStorage: file does not exist and createNewFile succeeds - writes file")
    void saveStorage_fileNotExists_createSucceeds_shouldWriteFile() throws Exception {
        //given
        setFilePath("file:/some/path/test_data.json");

        File mockFile = mock(File.class);
        File mockParent = mock(File.class);

        when(resourceLoader.getResource("file:/some/path/test_data.json")).thenReturn(resource);
        when(resource.getFile()).thenReturn(mockFile);
        when(mockFile.getParentFile()).thenReturn(mockParent);
        when(mockParent.exists()).thenReturn(true);
        when(mockFile.exists()).thenReturn(false);
        when(mockFile.createNewFile()).thenReturn(true);

        ObjectWriter mockWriter = mock(ObjectWriter.class);
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);

        //when
        initializer.saveStorage();

        //then
        verify(mockFile).createNewFile();
        verify(mockWriter).writeValue(eq(mockFile), any(StorageDTO.class));
    }

    @Test
    @DisplayName("saveStorage: parent dir exists - skips mkdirs, writes file")
    void saveStorage_validFile_shouldWriteFile() throws Exception {
        //given
        setFilePath("file:/some/path/test_data.json");

        File mockFile = mock(File.class);
        File mockParent = mock(File.class);

        when(resourceLoader.getResource("file:/some/path/test_data.json")).thenReturn(resource);
        when(resource.getFile()).thenReturn(mockFile);
        when(mockFile.getParentFile()).thenReturn(mockParent);
        when(mockParent.exists()).thenReturn(true);
        when(mockFile.exists()).thenReturn(true);

        ObjectWriter mockWriter = mock(ObjectWriter.class);
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);

        //when
        initializer.saveStorage();

        //then
        verify(mockParent, never()).mkdirs();
        verify(mockFile, never()).createNewFile();
        verify(mockWriter).writeValue(eq(mockFile), any(StorageDTO.class));
    }

    @Test
    @DisplayName("saveStorage: file does not exist and createNewFile fails - skips save")
    void saveStorage_fileNotExists_createFails_shouldDoNothing() throws Exception {
        //given
        setFilePath("file:/some/path/test_data.json");

        File mockFile = mock(File.class);
        File mockParent = mock(File.class);

        when(resourceLoader.getResource("file:/some/path/test_data.json")).thenReturn(resource);
        when(resource.getFile()).thenReturn(mockFile);
        when(mockFile.getParentFile()).thenReturn(mockParent);
        when(mockParent.exists()).thenReturn(true);
        when(mockFile.exists()).thenReturn(false);
        when(mockFile.createNewFile()).thenReturn(false);

        //when
        initializer.saveStorage();

        //then
        verify(mockFile).createNewFile();
        verifyNoInteractions(objectMapper);
    }
}