package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.persistence.UniProtRelease;
import org.zfin.uniprot.secondary.handlers.ActionCreator;
import org.zfin.uniprot.secondary.handlers.ActionProcessor;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;


//TODO: we should move the logic for processing actions into this class
@Getter
@Setter
@Log4j2
public class SecondaryTermLoadPipeline {
    private List<Tuple2<ActionCreator, Class<? extends ActionProcessor>>> handlerPairs = new ArrayList<>();
    private List<SecondaryTermLoadAction> actions = new ArrayList<>();
    private UniProtRelease release;
    private SecondaryLoadContext context;

    private Map<String, RichSequenceAdapter> uniprotRecords;

    public void addHandler(ActionCreator handler,
                           Class<? extends ActionProcessor> processHandler) {
        handlerPairs.add(new Tuple2<>(handler, processHandler));
    }

    public List<SecondaryTermLoadAction> createActions() {
        int i = 1;
        int actionCount;
        int previousActionCount = 0;

        for (var handlerPair : handlerPairs) {
            ActionCreator actionCreator = handlerPair.v1();
            Class<? extends ActionProcessor> actionProcessorClass = handlerPair.v2();

            log.debug("Starting action creation handler " + i + " of " + handlerPairs.size() + " (" + actionCreator.getClass().getName() + ")");

            actions.addAll(
                    associateActionsWithProcessor(
                        actionCreator.createActions(uniprotRecords, Collections.unmodifiableList(actions), context),
                        actionProcessorClass));

            actionCount = actions.size();
            log.debug("Finished action creation handler " + i + " of " + handlerPairs.size() + " (" + actionCreator.getClass().getName() + ")");

            if (actionCount == previousActionCount) {
                log.debug("No new actions were created by this handler");
            } else {
                log.debug("This handler created " + (actionCount - previousActionCount) + " new actions");
            }
            previousActionCount = actionCount;
            i++;
        }
        return actions;
    }

    private List<SecondaryTermLoadAction> associateActionsWithProcessor(List<SecondaryTermLoadAction> actions, Class<? extends ActionProcessor> actionProcessorClass) {
        return actions
                .stream()
                .map(action -> action.toBuilder().handlerClass(actionProcessorClass.getName()).build())
                .toList();
    }

    /**
     * Process the actions.
     * Some examples of actions from a run are below (count dbName type subType):
     *   33 InterPro DELETE DB_LINK
     *  851 InterPro LOAD DB_LINK
     *  306 EC LOAD DB_LINK
     *   15 Pfam DELETE DB_LINK
     *  332 Pfam LOAD DB_LINK
     *    9 PROSITE DELETE DB_LINK
     *  233 PROSITE LOAD DB_LINK
     *  635 InterPro LOAD MARKER_GO_TERM_EVIDENCE
     *   16 InterPro DELETE MARKER_GO_TERM_EVIDENCE
     *  400 EC LOAD MARKER_GO_TERM_EVIDENCE
     *  636 UniProtKB LOAD MARKER_GO_TERM_EVIDENCE
     *  124 UniProtKB DELETE MARKER_GO_TERM_EVIDENCE
     * 25983 null LOAD EXTERNAL_NOTE
     */
    public void processActions() {
        //groupBy the actions
        Map<SecondaryTermLoadAction.Type, List<SecondaryTermLoadAction>> groupedByType = actions.stream().collect(Collectors.groupingBy(SecondaryTermLoadAction::getType));

        //only perform actions for LOAD and DELETE for now
        //LOADs come first (though maybe it doesn't matter?)
        List<SecondaryTermLoadAction.Type> typesOfActions = new ArrayList<>();
        if (groupedByType.containsKey(SecondaryTermLoadAction.Type.LOAD)) {
            typesOfActions.add(SecondaryTermLoadAction.Type.LOAD);
        }
        if (groupedByType.containsKey(SecondaryTermLoadAction.Type.DELETE)) {
            typesOfActions.add(SecondaryTermLoadAction.Type.DELETE);
        }

        //process the actions
        currentSession().beginTransaction();
        for(SecondaryTermLoadAction.Type type : typesOfActions) {
            log.debug("Processing action types of " + type);
            List<SecondaryTermLoadAction> transactionActions = groupedByType.get(type);
            processLoadActions(type, transactionActions);
            log.debug("Finished action types of " + type);
        }
        if (release != null) {
            if (release.getProcessedDate() == null) {
                log.error("Release ID# " + release.getUpr_id() + " has not been processed yet. Must process before secondary terms.");
                currentSession().getTransaction().rollback();
                System.exit(5);
            }
            release.setSecondaryLoadDate(new Date());
            getInfrastructureRepository().updateUniProtRelease(release);
        }
        log.debug("Committing changes");
        currentSession().getTransaction().commit();
    }

    private static void processLoadActions(SecondaryTermLoadAction.Type type, List<SecondaryTermLoadAction> transactionActions) {
        //groupBy the actions by subtype
        Map<SecondaryTermLoadAction.SubType, List<SecondaryTermLoadAction>> groupedBySubType =
                transactionActions.stream().collect(Collectors.groupingBy(SecondaryTermLoadAction::getSubType));

        //sort by SubType.ordinal
        List<SecondaryTermLoadAction.SubType> orderedSubTypes =
                groupedBySubType.keySet().stream().sorted(Comparator.comparingInt(subtype -> subtype.getProcessActionOrder())).toList();

        for(SecondaryTermLoadAction.SubType subType : orderedSubTypes) {
            List<SecondaryTermLoadAction> subTypeActions = groupedBySubType.get(subType);
            processLoadActionsBySubType(subType, subTypeActions);
        }
    }

    private static void processLoadActionsBySubType(SecondaryTermLoadAction.SubType subType, List<SecondaryTermLoadAction> subTypeActions) {
        String handlerClass = subTypeActions.get(0).getHandlerClass();
        ActionProcessor handler = getHandler(handlerClass);
        if (handler.isSubTypeHandlerFor() != subType) {
            throw new RuntimeException("Handler class " + handlerClass + " does not support subType " + subType);
        }
        log.debug("Processing " + subTypeActions.size() + " actions for " + subType + " using handler " + handlerClass);
        Date timestamp = new Date();
        handler.processActions(subTypeActions);
        long timeElapsed = new Date().getTime() - timestamp.getTime();
        long secondsElapsed = timeElapsed / 1000;
        log.debug("Finished processing " + subTypeActions.size() + " actions in " + secondsElapsed + " seconds for " + subType + " using handler " + handlerClass);
    }

    /**
     * Use reflection to get an instance of the class that handles the actions.
     * @param handlerClass the class name
     * @return the handler
     */
    private static ActionProcessor getHandler(String handlerClass) {
        try {
            Class<?> cls = Class.forName(handlerClass);
            Object instance = cls.getDeclaredConstructor().newInstance();
            return (ActionProcessor) instance;
        } catch (ClassNotFoundException e) {
            // Handle the case where the class doesn't exist
            e.printStackTrace();
            throw new RuntimeException("No such handler class: " + handlerClass);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            // Handle other potential exceptions
            e.printStackTrace();
            throw new RuntimeException("Could not instantiate handler class: " + handlerClass);
        }
    }

}
