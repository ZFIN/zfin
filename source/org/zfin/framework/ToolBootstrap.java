package org.zfin.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Transaction;
import org.zfin.properties.ZfinProperties;

/**
 * Headless entry point for ad-hoc Java jobs (reports, audits, one-off data fixes) that need to talk
 * to the database without a full webapp deploy.
 * <p>
 * It removes the boilerplate every command-line job otherwise repeats: load {@link ZfinProperties},
 * register a {@link org.hibernate.SessionFactory} with {@link HibernateUtil} via
 * {@link HibernateSessionCreator}, run the work inside a single transaction, and always close the
 * session. Once {@link #run} has set this up, jobs use the rest of the codebase normally &mdash;
 * {@code RepositoryFactory.getXxxRepository()}, the static service classes, {@code ZfinPropertiesEnum},
 * and {@code AbstractZfinMailSender.getInstance()} all work, because none of those depend on a Spring
 * context (ZFIN's data layer is static-factory based, and it uses no {@code @Transactional}/Spring-AOP).
 * <p>
 * Typical usage from a {@code main()}:
 * <pre>{@code
 * public static void main(String[] args) {
 *     ToolBootstrap.run("OrthoInconsistencyReport", args, () -> {
 *         OrthologyRepository repo = RepositoryFactory.getOrthologyRepository();
 *         // ...query, build the report, write the file...
 *     });
 * }
 * }</pre>
 *
 * @see HibernateSessionCreator
 * @see HibernateUtil
 */
public final class ToolBootstrap {

    private static final Logger LOG = LogManager.getLogger(ToolBootstrap.class);

    private ToolBootstrap() {
    }

    /**
     * A unit of work to run against a bootstrapped ZFIN environment. May throw any checked exception;
     * it will be caught, the transaction rolled back, and a {@link RuntimeException} rethrown.
     */
    @FunctionalInterface
    public interface ToolJob {
        void run() throws Exception;
    }

    /**
     * Bootstrap the environment and run {@code job} inside a single managed transaction.
     * <p>
     * Lifecycle: {@link #initialize()} &rarr; begin transaction &rarr; {@code job.run()} &rarr; flush
     * and commit on success / roll back on any exception &rarr; close the Hibernate session (always).
     * If the job manages its own transactions (e.g. incremental commits via
     * {@link HibernateUtil#flushAndCommitCurrentSession()}), the wrapper only commits when the
     * transaction it opened is still active, so it will not double-commit.
     *
     * @param jobName label used in log lines
     * @param job     the work to perform
     * @throws RuntimeException wrapping any exception thrown by the job (after rollback)
     */
    public static void run(String jobName, ToolJob job) {
        long start = System.currentTimeMillis();
        LOG.info("Starting tool '{}'", jobName);
        initialize();
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            job.run();
            if (tx.isActive()) {
                HibernateUtil.currentSession().flush();
                tx.commit();
            }
        } catch (Exception e) {
            LOG.error("Tool '{}' failed; rolling back", jobName, e);
            if (tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackError) {
                    LOG.error("Rollback failed for tool '{}'", jobName, rollbackError);
                }
            }
            throw new RuntimeException("Tool '" + jobName + "' failed: " + e.getMessage(), e);
        } finally {
            HibernateUtil.closeSession();
            LOG.info("Tool '{}' finished in {} ms", jobName, System.currentTimeMillis() - start);
        }
    }

    /**
     * Convenience overload that logs the command-line arguments before delegating to
     * {@link #run(String, ToolJob)}. The arguments are not passed into the job &mdash; the job's
     * {@code main()} already has them and can close over whatever it needs.
     */
    public static void run(String jobName, String[] args, ToolJob job) {
        LOG.info("Tool '{}' args: {}", jobName, (args == null ? "[]" : String.join(" ", args)));
        run(jobName, job);
    }

    /**
     * Load {@link ZfinProperties} and ensure a Hibernate {@link org.hibernate.SessionFactory} is
     * registered with {@link HibernateUtil}. Idempotent: a second call is a no-op once the
     * SessionFactory exists.
     * <p>
     * Exposed for jobs that want to manage their own transaction boundaries (e.g. long-running loads
     * that commit in batches) rather than the single-transaction wrapper that {@link #run} provides.
     */
    public static void initialize() {
        ZfinProperties.init();
        if (!HibernateUtil.hasSessionFactoryDefined()) {
            new HibernateSessionCreator();
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Spring-context variant -- NOT YET IMPLEMENTED.
    //
    // The vast majority of ZFIN jobs do NOT need a Spring ApplicationContext: repositories come from
    // RepositoryFactory, services are static, mail is AbstractZfinMailSender.getInstance(), and
    // properties are ZfinPropertiesEnum. The codebase uses no @Transactional / @Cacheable / @Async /
    // @Scheduled / method-security, so there is no Spring-AOP behavior to recover either.
    //
    // A context only earns its keep for the rare case of a bean wired ONLY through @Autowired with no
    // static-factory accessor (the existing example is GafLoadJob, which boots its own
    // FileSystemXmlApplicationContext to get an @Autowired DownloadService plus pluggable parser beans).
    //
    // Implementation notes for whoever picks this up (verified empirically 2026-06):
    //   * Order matters: call initialize() FIRST (Hibernate up), THEN build the context. Beans whose
    //     construction touches HibernateUtil.currentSession() fail otherwise.
    //   * You CANNOT use ZfinConfiguration's plain @ComponentScan("org.zfin") headlessly: it sweeps in
    //     web @Controllers (e.g. anatomyAjaxController) that @Autowired a request-scoped
    //     HttpServletRequest, which does not exist outside a servlet container, and the refresh fails
    //     with UnsatisfiedDependencyException.
    //   * Define a dedicated headless @Configuration that excludes the web layer, e.g.:
    //         @ComponentScan(value = "org.zfin", excludeFilters = @ComponentScan.Filter(
    //             type = FilterType.ANNOTATION,
    //             classes = {Controller.class, RestController.class, ControllerAdvice.class}))
    //     then iterate the exclude set against whatever fails next (likely servlet-backed GWT RPC
    //     @Services) until the context refreshes cleanly. Log every exclusion so coverage stays honest.
    //   * Wrap the same transaction lifecycle as run(), and close() the context in a finally block.
    //
    // Intended shape:
    //
    //   @FunctionalInterface
    //   public interface ToolJobWithContext {
    //       void run(org.springframework.context.ApplicationContext ctx) throws Exception;
    //   }
    //
    //   public static void runWithContext(String jobName, String[] args, ToolJobWithContext job) { ... }
    // ------------------------------------------------------------------------------------------------

    /**
     * Placeholder for a future Spring-context-backed entry point. Not implemented &mdash; see the
     * design notes in the source above this method. Use {@link #run(String, ToolJob)} instead unless
     * you have a concrete bean that can only be obtained through {@code @Autowired} wiring.
     *
     * @throws UnsupportedOperationException always
     */
    public static void runWithContext(String jobName, String[] args, Object job) {
        throw new UnsupportedOperationException(
                "ToolBootstrap.runWithContext is not implemented. Almost all jobs only need run(...). "
                        + "A Spring context is only required for @Autowired-only bean graphs; see the "
                        + "design notes in ToolBootstrap.java before implementing.");
    }
}
