package org.zfin.framework;

import org.apache.log4j.Logger;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchOntologyRefresh {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private boolean trace = false;

    private static final Logger LOG = Logger.getLogger(WatchOntologyRefresh.class);

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                LOG.info("Registering Ontology Reload Directory: " + dir);
            }
        }
        keys.put(key, dir);
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    public WatchOntologyRefresh(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        register(dir);

        // enable trace after initial registration
        this.trace = true;
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
                    Ontology ontology = Ontology.getOntology(name.toString());
                    // only process files with the ontology name
                    if (ontology != null){
                        OntologyManager.getInstance().reloadOntology(ontology);
                        child.toFile().delete();
                    }
                    HibernateUtil.closeSession();
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
        new WatchOntologyRefresh(dir).processEvents();
    }
}