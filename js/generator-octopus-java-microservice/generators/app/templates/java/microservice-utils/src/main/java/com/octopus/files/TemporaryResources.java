package com.octopus.files;

import com.octopus.exceptions.TemporaryResourceException;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Utility class for tracking and ultimately closing or otherwise disposing
 * a collection of temporary resources.
 *
 * <p>Note that this class is not thread-safe.
 *
 * <p>Source: https://github.com/apache/tika/blob/main/tika-core/src/main/java/org/apache/tika/io/TemporaryResources.java
 */
public class TemporaryResources implements Closeable {

  private static Logger LOG = Logger.getLogger(TemporaryResources.class.getName());

  /**
   * Tracked resources in LIFO order.
   */
  private final LinkedList<Closeable> resources = new LinkedList<>();

  /**
   * Directory for temporary files, <code>null</code> for the system default.
   */
  private Path tempFileDir = null;

  /**
   * Sets the directory to be used for the temporary files created by
   * the {@link #createTempFile(String, String)} method.
   *
   * @param tempFileDir temporary file directory,
   *                    or <code>null</code> for the system default
   */
  public void setTemporaryFileDirectory(Path tempFileDir) {
    this.tempFileDir = tempFileDir;
  }

  /**
   * Sets the directory to be used for the temporary files created by
   * the {@link #createTempFile(String, String)} method.
   *
   * @param tempFileDir temporary file directory,
   *                    or <code>null</code> for the system default
   * @see #setTemporaryFileDirectory(Path)
   */
  public void setTemporaryFileDirectory(File tempFileDir) {
    this.tempFileDir = tempFileDir == null ? null : tempFileDir.toPath();
  }

  /**
   * Creates a temporary directory that will automatically be deleted when
   * the {@link #close()} method is called, returning its path.
   *
   * @return Path to created temporary directory that will be deleted after closing
   * @throws IOException Failed to create temporary directory.
   */
  public Path createTempDirectory(final String prefix) throws IOException {
    final Path path = tempFileDir == null ? Files.createTempDirectory(prefix) :
        Files.createTempDirectory(tempFileDir, prefix);
    addResource(new Closeable() {
      public void close() throws IOException {
        try {
          Files.delete(path);
        } catch (IOException e) {
          // delete when exit if current delete fail
          LOG.log(Level.WARNING, "delete tmp file fail, will delete it on exit");
          path.toFile().deleteOnExit();
        }
      }
    });
    return path;
  }

  /**
   * Creates and returns a temporary directory that will automatically be
   * deleted when the {@link #close()} method is called.
   *
   * @return Created temporary directory that'll be deleted after closing
   * @throws IOException Failed to create temporary directory.
   * @see #createTempDirectory(String)
   */
  public File createTemporaryDir(final String prefix) throws IOException {
    return createTempDirectory(prefix).toFile();
  }

  /**
   * Creates a temporary file that will automatically be deleted when
   * the {@link #close()} method is called, returning its path.
   *
   * @return Path to created temporary file that will be deleted after closing
   * @throws IOException Failed to create temporary file.
   */
  public Path createTempFile(final String prefix, final String suffix) throws IOException {
    final Path path = tempFileDir == null ? Files.createTempFile(prefix, suffix) :
        Files.createTempFile(tempFileDir, prefix, suffix);
    addResource(new Closeable() {
      public void close() throws IOException {
        try {
          Files.delete(path);
        } catch (IOException e) {
          // delete when exit if current delete fail
          LOG.log(Level.WARNING, "delete tmp file fail, will delete it on exit");
          path.toFile().deleteOnExit();
        }
      }
    });
    return path;
  }

  /**
   * Creates and returns a temporary file that will automatically be
   * deleted when the {@link #close()} method is called.
   *
   * @return Created temporary file that'll be deleted after closing
   * @throws IOException Failed to create temporary file.
   * @see #createTempFile(String, String)
   */
  public File createTemporaryFile(final String prefix, final String suffix) throws IOException {
    return createTempFile(prefix, suffix).toFile();
  }

  /**
   * Adds a new resource to the set of tracked resources that will all be
   * closed when the {@link #close()} method is called.
   *
   * @param resource resource to be tracked
   */
  public void addResource(Closeable resource) {
    resources.addFirst(resource);
  }

  /**
   * Returns the latest of the tracked resources that implements or
   * extends the given interface or class.
   *
   * @param klass interface or class
   * @return matching resource, or <code>null</code> if not found
   */
  @SuppressWarnings("unchecked")
  public <T extends Closeable> T getResource(Class<T> klass) {
    for (Closeable resource : resources) {
      if (klass.isAssignableFrom(resource.getClass())) {
        return (T) resource;
      }
    }
    return null;
  }

  /**
   * Closes all tracked resources. The resources are closed in reverse order
   * from how they were added.
   *
   * <p>Any suppressed exceptions from managed resources are collected and
   * then added to the first thrown exception, which is re-thrown once
   * all the resources have been closed.
   *
   * @throws IOException if one or more of the tracked resources
   *                     could not be closed
   */
  public void close() throws IOException {
    // Release all resources and keep track of any exceptions
    IOException exception = null;
    for (Closeable resource : resources) {
      try {
        resource.close();
      } catch (IOException e) {
        if (exception == null) {
          exception = e;
        } else {
          exception.addSuppressed(e);
        }
      }
    }
    resources.clear();

    // Throw any exceptions that were captured from above
    if (exception != null) {
      throw exception;
    }
  }

  /**
   * Calls the {@link #close()} method and wraps the potential
   * {@link IOException} into a {@link TemporaryResourceException} for convenience
   * when used within Tika.
   *
   * @throws TemporaryResourceException if one or more of the tracked resources
   *                       could not be closed
   */
  public void dispose() throws TemporaryResourceException {
    try {
      close();
    } catch (IOException e) {
      throw new TemporaryResourceException("Failed to close temporary resources", e);
    }
  }

}