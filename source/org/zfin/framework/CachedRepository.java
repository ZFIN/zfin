package org.zfin.framework;

/**
 * Base interface for repositories. This is a reminder to make use of object caching
 * and also provide a means to invalidate a cache if needed.
 */
public interface CachedRepository {

    /**
     * This method should invlidate all chached obejcts if there are any used for performance
     * reasons. Call this method for infrequent updates, such as stage or AO imports.
     */
    void invalidateCachedObjects();

}
