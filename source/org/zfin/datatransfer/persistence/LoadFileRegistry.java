package org.zfin.datatransfer.persistence;

import org.hibernate.Session;

/**
 * Finds the {@link LoadFile} a load run belongs to, adding the {@link Load} and the file the first time
 * they are seen, so a new load or a new source is registered without a migration. A file that is added
 * this way is dumped with its rows (the default); switching that off is a deliberate change to the data.
 *
 * <p>Works in the caller's session and transaction; it flushes only to make the new row visible to a
 * query in the same session.
 */
public final class LoadFileRegistry {

    private LoadFileRegistry() {
    }

    /**
     * @param tableName the schema-qualified table the file is parsed into, or null. Filled in on an existing
     *                  file that has none; an existing value is never changed.
     */
    public static LoadFile getOrCreate(Session session, String loadName, String source, String tableName) {
        Load load = session.createQuery("from Load where name = :name", Load.class)
                .setParameter("name", loadName)
                .setMaxResults(1)
                .uniqueResult();
        if (load == null) {
            load = new Load();
            load.setName(loadName);
            session.persist(load);
            session.flush();
        }

        LoadFile file = session.createQuery("from LoadFile where load = :load and source = :source", LoadFile.class)
                .setParameter("load", load)
                .setParameter("source", source)
                .setMaxResults(1)
                .uniqueResult();
        if (file == null) {
            file = new LoadFile();
            file.setLoad(load);
            file.setSource(source);
            file.setTableName(tableName);
            session.persist(file);
            session.flush();
        } else if (file.getTableName() == null && tableName != null) {
            file.setTableName(tableName);
        }
        return file;
    }
}
