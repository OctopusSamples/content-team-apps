package com.octopus.files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

public class TemporaryResourcesTest {
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

  private static Stream<Arguments> provideTempPaths() {
    return Stream.of(
        Arguments.of(Path.of(TEMP_DIR))
    );
  }

  @ParameterizedTest
  @NullSource
  @MethodSource("provideTempPaths")
  public void testTempFileDeletion(final Path tempDir) throws IOException {
    Path tempFile;
    try (TemporaryResources tempResources = new TemporaryResources()) {
      tempResources.setTemporaryFileDirectory(tempDir);
      tempFile = tempResources.createTempFile("test", ".tmp");
      assertTrue(Files.exists(tempFile), "Temp file should exist while TempResources is used");
      assertNull(tempResources.getResource(ClosableTest.class), "No resources of this type were added, so the result must be null");
    }
    assertTrue(Files.notExists(tempFile),
        "Temp file should not exist after TempResources is closed");
  }

  @ParameterizedTest
  @NullSource
  @MethodSource("provideTempPaths")
  public void testTemporaryFileDeletion(final Path tempDir) throws IOException {
    File tempFile;
    try (TemporaryResources tempResources = new TemporaryResources()) {
      tempResources.setTemporaryFileDirectory(tempDir == null ? null : tempDir.toFile());
      tempFile = tempResources.createTemporaryFile("test", ".tmp");
      assertTrue(tempFile.exists(), "Temp file should exist while TempResources is used");
    }
    assertFalse(tempFile.exists(), "Temp file should not exist after TempResources is closed");
  }

  @ParameterizedTest
  @NullSource
  @MethodSource("provideTempPaths")
  public void testTempDirectoryDeletion(final Path tempDir) throws IOException {
    Path tempFile;
    try (TemporaryResources tempResources = new TemporaryResources()) {
      tempResources.setTemporaryFileDirectory(tempDir);
      tempFile = tempResources.createTempDirectory("test");
      assertTrue(Files.exists(tempFile), "Temp file should exist while TempResources is used");
    }
    assertTrue(Files.notExists(tempFile),
        "Temp file should not exist after TempResources is closed");
  }

  @ParameterizedTest
  @NullSource
  @MethodSource("provideTempPaths")
  public void testTempDirDeletion(final Path tempDir) throws IOException {
    File tempFile;
    try (TemporaryResources tempResources = new TemporaryResources()) {
      tempResources.setTemporaryFileDirectory(tempDir == null ? null : tempDir.toFile());
      tempFile = tempResources.createTemporaryDir("test");
      assertTrue(tempFile.exists(), "Temp file should exist while TempResources is used");
    }
    assertFalse(tempFile.exists(), "Temp file should not exist after TempResources is closed");
  }

  @Test
  public void testClosableResources() throws IOException {
    final ClosableTest closableTest = new ClosableTest();
    try (TemporaryResources tempResources = new TemporaryResources()) {
      tempResources.addResource(closableTest);
      assertNotNull(tempResources.getResource(ClosableTest.class), "The closable resource must be tracked by TemporaryResources");
      assertNull(tempResources.getResource(ClosableExceptionTest.class), "No resource of this type was added, so the result must be null");
    }
    assertTrue(closableTest.isClosed,
        "The closable resource must have been closed");
  }

  @Test
  public void testClosableExceptionResources() {
    assertThrows(IOException.class, () -> {
      final ClosableExceptionTest closableTest = new ClosableExceptionTest();
      try (TemporaryResources tempResources = new TemporaryResources()) {
        tempResources.addResource(closableTest);
      }
    });
  }

  @Test
  public void testMultipleClosableExceptionResources() {
    assertThrows(IOException.class, () -> {
      final ClosableExceptionTest closableTest = new ClosableExceptionTest();
      final ClosableExceptionTest closableTest2 = new ClosableExceptionTest();
      try (TemporaryResources tempResources = new TemporaryResources()) {
        tempResources.addResource(closableTest);
        tempResources.addResource(closableTest2);
      }
    });
  }

  private static class ClosableTest implements Closeable {
    public boolean isClosed = false;

    @Override
    public void close() {
      isClosed = true;
    }
  }

  private static class ClosableExceptionTest implements Closeable {
    @Override
    public void close() throws IOException {
      throw new IOException();
    }
  }
}
