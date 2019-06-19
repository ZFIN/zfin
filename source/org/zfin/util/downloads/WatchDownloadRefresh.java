package org.zfin.util.downloads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchDownloadRefresh {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;

    private static final Logger LOG = LogManager.getLogger(WatchDownloadRefresh.class);

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Path prev = keys.get(key);
        if (prev == null) {
            LOG.info("Registering Download Files Reload Directory: " + dir);
        }
        keys.put(key, dir);
    }

    private DownloadFileService downloadFileService;

    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchDownloadRefresh(DownloadFileService downloadFileService, Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.downloadFileService = downloadFileService;
        register(dir);
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                LOG.error("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                LOG.info(event.kind().name() + ": " + child);

                if (kind == ENTRY_CREATE) {
                    child.toFile().delete();
                    downloadFileService.updateCache();
                    System.out.println("New file in reload-status found. Re-loading download cache");
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // register directory and process its events
        Path dir = Paths.get(args[0]);
        new WatchDownloadRefresh(new DownloadFileService(), dir).processEvents();
    }
}