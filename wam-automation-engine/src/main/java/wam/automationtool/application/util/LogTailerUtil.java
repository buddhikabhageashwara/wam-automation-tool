package wam.automationtool.application.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import wam.automationtool.application.dto.testcasestep.LogFileBase64Dto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogTailerUtil {

    /**
     * Polling interval (ms) for checking new bytes.
     * Lower values reduce latency but can increase cpu usage.
     */
    private static final long DEFAULT_DELAY_MILLIS = 100L;

    /** Temp folder name under java.io.tmpdir. */
    private static final String TEMP_DIR_NAME = "tcs-reading-logs";

    /** Auto stop time if stop is not called (2 hours). */
    private static final long AUTO_STOP_MILLIS = 2L * 60L * 60L * 1000L;

    private static final ScheduledExecutorService AUTO_STOP_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                final Thread thread = new Thread(r, "tcs-log-tailer-auto-stop");
                thread.setDaemon(true);
                return thread;
            });

    /**
     * Keeps active tailers by handle key (tempLogFileName).
     */
    private static final ConcurrentMap<String, TailerHandle> ACTIVE_TAILERS = new ConcurrentHashMap<>();

    /**
     * Starts tailing {@code logFileLocation} and writes tailed lines to a temp file
     * whose name is exactly {@code tempLogFileName}.
     *
     * @param handleKey        unique key to identify this tailing session (use tempLogFileName).
     * @param logFileLocation  absolute/relative path of the log file to read.
     * @return the full path of the temp output file.
     * @throws LogTailingException if validation fails or tailing cannot be started.
     */
    public static Path startReading(final String handleKey, final String logFileLocation) {
        try {
            validateStartParams(handleKey, logFileLocation);
            stopReadingIfExists(handleKey);
            final Path tempDirPath = ensureTempDirExists();
            final Path tempOutputFilePath = tempDirPath.resolve(handleKey);
            recreateFile(tempOutputFilePath);
            final BufferedWriter writer =
                    Files.newBufferedWriter(
                            tempOutputFilePath,
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);
            final File sourceFile = Paths.get(logFileLocation).toFile();
            final TailerListenerAdapter listener = new WritingTailerListener(handleKey, writer);
            final Tailer tailer = new Tailer(
                    sourceFile, listener, DEFAULT_DELAY_MILLIS, true, true, 4096);
            final Thread tailerThread = new Thread(tailer, "tcs-log-tailer-" + handleKey);
            tailerThread.setDaemon(true);
            final ScheduledFuture<?> autoStopFuture = AUTO_STOP_SCHEDULER.schedule(() -> {
                try {
                    log.info("log tailing auto-stop triggered:" +
                            " handleKey: {}, afterMillis: {}", handleKey, AUTO_STOP_MILLIS);
                    stopReading(handleKey);
                } catch (final Exception exception) {
                    log.error("log tailing auto-stop failed: handleKey: {}, reason: {}", handleKey, safeMsg(exception));
                }
            }, AUTO_STOP_MILLIS, TimeUnit.MILLISECONDS);
            ACTIVE_TAILERS.put(handleKey, new TailerHandle(
                    handleKey, tailer, tailerThread, writer, tempOutputFilePath, autoStopFuture));
            tailerThread.start();
            log.info("log tailing started: handleKey: {}, source: {}, temp: {}, delayMillis: {}",
                    handleKey, logFileLocation, tempOutputFilePath.toAbsolutePath(), DEFAULT_DELAY_MILLIS);
            return tempOutputFilePath;
        } catch (final Exception exception) {
            log.error("log tailing start failed: handleKey: {}, reason: {}", handleKey, safeMsg(exception));
            throw new LogTailingException("failed to start log tailing: " + safeMsg(exception), exception);
        }
    }

    public static LogFileBase64Dto getTempFileAsBase64(final String handleKey) {
        try {
            if (Objects.isNull(handleKey) || handleKey.isBlank()) {
                throw new LogTailingException("handleKey is null or blank");
            }
            final Path tempDirPath = ensureTempDirExists();
            final Path tempOutputFilePath = tempDirPath.resolve(handleKey);
            if (!Files.exists(tempOutputFilePath)) {
                throw new LogTailingException("temp log file not found: " + tempOutputFilePath.toAbsolutePath());
            }
            if (!Files.isRegularFile(tempOutputFilePath)) {
                throw new LogTailingException("temp log file path is not a file: " +
                        tempOutputFilePath.toAbsolutePath());
            }
            if (!Files.isReadable(tempOutputFilePath)) {
                throw new LogTailingException("temp log file is not readable: " + tempOutputFilePath.toAbsolutePath());
            }
            final byte[] fileBytes = Files.readAllBytes(tempOutputFilePath);
            final String base64 = Base64.getEncoder().encodeToString(fileBytes);
            final String fileName = tempOutputFilePath.getFileName().toString();
            final String extension = extractExtension(fileName);
            final long sizeBytes = Files.size(tempOutputFilePath);
            return LogFileBase64Dto.builder()
                    .handleKey(handleKey)
                    .fileName(fileName)
                    .extension(extension)
                    .tempLogFileLocation(tempOutputFilePath.toAbsolutePath().toString())
                    .sizeBytes(sizeBytes)
                    .base64(base64)
                    .build();
        } catch (final Exception exception) {
            log.error("temp log file base64 read failed: handleKey: {}, reason: {}", handleKey, safeMsg(exception));
            throw new LogTailingException("failed to read temp log file as base64: " + safeMsg(exception), exception);
        }
    }

    private static String extractExtension(final String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank()) {
            return "";
        }
        final int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }

    /**
     * Deletes the temp output file for the given {@code handleKey}.
     *
     * <p>If tailing is still running for the key, it will be stopped first.
     *
     * @param handleKey unique key used when starting (tempLogFileName).
     * @throws LogTailingException if deletion fails.
     */
    public static void deleteTempFile(final String handleKey) {
        try {
            if (Objects.isNull(handleKey) || handleKey.isBlank()) {
                throw new LogTailingException("handleKey is null or blank");
            }
            stopReadingIfExists(handleKey);
            final Path tempDirPath = ensureTempDirExists();
            final Path tempOutputFilePath = tempDirPath.resolve(handleKey);
            if (!Files.exists(tempOutputFilePath)) {
                log.info("temp log file delete skipped: handleKey: {}, reason: file not found", handleKey);
                return;
            }
            Files.delete(tempOutputFilePath);
            log.info("temp log file deleted: handleKey: {}, temp: {}", handleKey, tempOutputFilePath.toAbsolutePath());
        } catch (final Exception exception) {
            log.error("temp log file delete failed: handleKey: {}, reason: {}", handleKey, safeMsg(exception));
            throw new LogTailingException("failed to delete temp log file: " + safeMsg(exception), exception);
        }
    }

    /**
     * Stops tailing for the given {@code handleKey} (tempLogFileName) and closes resources.
     *
     * @param handleKey unique key used when starting (tempLogFileName).
     * @throws LogTailingException if stop fails.
     */
    public static void stopReading(final String handleKey) {
        try {
            if (Objects.isNull(handleKey) || handleKey.isBlank()) {
                throw new LogTailingException("handleKey is null or blank");
            }
            final TailerHandle handle = ACTIVE_TAILERS.remove(handleKey);
            if (Objects.isNull(handle)) {
                log.info("log tailing stop skipped: handleKey: {}, reason: not running", handleKey);
                return;
            }
            if (Objects.nonNull(handle.autoStopFuture)) {
                handle.autoStopFuture.cancel(false);
            }
            handle.tailer.stop();
            try {
                handle.thread.join(2000L);
            } catch (final InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                log.warn("log tailing stop interrupted: handleKey: {}", handleKey);
            }
            closeQuietly(handle.writer);
            log.info("log tailing stopped: handleKey: {}, temp: {}", handleKey, handle.tempFilePath.toAbsolutePath());
        } catch (final Exception exception) {
            log.error("log tailing stop failed: handleKey: {}, reason: {}", handleKey, safeMsg(exception));
            throw new LogTailingException("failed to stop log tailing: " + safeMsg(exception), exception);
        }
    }

    private static void validateStartParams(final String handleKey, final String logFileLocation) {
        if (Objects.isNull(handleKey) || handleKey.isBlank()) {
            throw new LogTailingException("handleKey is null or blank");
        }
        if (Objects.isNull(logFileLocation) || logFileLocation.isBlank()) {
            throw new LogTailingException("logFileLocation is null or blank");
        }
        final Path sourcePath = Paths.get(logFileLocation);
        if (!Files.exists(sourcePath)) {
            throw new LogTailingException("log file does not exist: " + sourcePath.toAbsolutePath());
        }
        if (!Files.isRegularFile(sourcePath)) {
            throw new LogTailingException("log file path is not a file: " + sourcePath.toAbsolutePath());
        }
        if (!Files.isReadable(sourcePath)) {
            throw new LogTailingException("log file is not readable: " + sourcePath.toAbsolutePath());
        }
    }

    private static void stopReadingIfExists(final String handleKey) {
        final TailerHandle existing = ACTIVE_TAILERS.get(handleKey);
        if (Objects.nonNull(existing)) {
            log.info("log tailing already running: handleKey: {}, action: stopping existing tailer", handleKey);
            stopReading(handleKey);
        }
    }

    private static Path ensureTempDirExists() throws IOException {
        final String tmpRoot = System.getProperty("java.io.tmpdir");
        final Path tempDirPath = Paths.get(tmpRoot, TEMP_DIR_NAME);
        Files.createDirectories(tempDirPath);
        return tempDirPath;
    }

    private static void recreateFile(final Path path) throws IOException {
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Files.createFile(path);
    }

    private static void closeQuietly(final BufferedWriter writer) {
        if (Objects.isNull(writer)) {
            return;
        }
        try {
            writer.flush();
            writer.close();
        } catch (final Exception exception) {
            log.warn("writer close failed: reason: {}", safeMsg(exception));
        }
    }

    private static String safeMsg(final Throwable throwable) {
        final String message = throwable.getMessage();
        return (Objects.isNull(message) || message.isBlank()) ? throwable.getClass().getSimpleName() : message;
    }

    private static final class WritingTailerListener extends TailerListenerAdapter {
        private final String handleKey;
        private final BufferedWriter writer;
        private WritingTailerListener(final String handleKey, final BufferedWriter writer) {
            this.handleKey = handleKey;
            this.writer = writer;
        }

        @Override
        public void handle(final String line) {
            try {
                writer.write(line);
                writer.newLine();
                writer.flush();
            } catch (final Exception exception) {
                log.error("log write failed: handleKey: {}, reason: {}", handleKey, safeMsg(exception));
                throw new LogTailingException("failed to write tailed log line: " + safeMsg(exception), exception);
            }
        }

        @Override
        public void fileNotFound() {
            log.error("log file not found while tailing: handleKey: {}", handleKey);
            throw new LogTailingException("log file not found while tailing: handleKey: " + handleKey);
        }

        @Override
        public void fileRotated() {
            log.info("log file rotated: handleKey: {}, time: {}", handleKey, Instant.now());
        }

        @Override
        public void handle(final Exception exception) {
            log.error("tailer listener error: handleKey: {}, reason: {}", handleKey, safeMsg(exception));
            throw new LogTailingException("tailer listener error: " + safeMsg(exception), exception);
        }
    }

    private record TailerHandle(
            String handleKey,
            Tailer tailer,
            Thread thread,
            BufferedWriter writer,
            Path tempFilePath,
            ScheduledFuture<?> autoStopFuture
    ) { }

    public static final class LogTailingException extends RuntimeException {
        public LogTailingException(final String message) {
            super(message);
        }
        public LogTailingException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}

