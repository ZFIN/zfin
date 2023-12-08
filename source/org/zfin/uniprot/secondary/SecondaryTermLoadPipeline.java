package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
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
    private List<Pair<ActionCreator, Class<? extends ActionProcessor>>> handlerPairs = new ArrayList<>();
    private List<SecondaryTermLoadAction> actions = new ArrayList<>();
    private UniProtRelease release;
    private SecondaryLoadContext context;

    private UniprotReleaseRecords uniprotRecords;

    public void addHandler(ActionCreator handler,
                           Class<? extends ActionProcessor> processHandler) {
        handlerPairs.add(new ImmutablePair<>(handler, processHandler));
    }

    public List<SecondaryTermLoadAction> createActions() {
        int i = 1;
        int actionCount;
        int previousActionCount = 0;

        for (var handlerPair : handlerPairs) {
            ActionCreator actionCreator = handlerPair.getLeft();
            Class<? extends ActionProcessor> actionProcessorClass = handlerPair.getRight();

            log.info("Starting action creation handler " + i + " of " + handlerPairs.size() + " (" + actionCreator.getClass().getName() + ")");

            List<SecondaryTermLoadAction> calculatedActions =
                actionCreator.createActions(uniprotRecords, Collections.unmodifiableList(actions), context);
            List<SecondaryTermLoadAction> actionsWithProcessor = associateActionsWithProcessor(calculatedActions, actionProcessorClass);
            actions.addAll(actionsWithProcessor);

            actionCount = actions.size();
            log.info("Finished action creation handler " + i + " of " + handlerPairs.size() + " (" + actionCreator.getClass().getName() + ")");

            if (actionCount == previousActionCount) {
                log.info("No new actions were created by this handler");
            } else {
                log.info("This handler created " + (actionCount - previousActionCount) + " new actions");
            }
            previousActionCount = actionCount;
            i++;
        }
        return actions;
    }

    private List<SecondaryTermLoadAction> associateActionsWithProcessor(List<SecondaryTermLoadAction> actions, Class<? extends ActionProcessor> actionProcessorClass) {
        return actions
                .stream()
                .map(action -> action.toBuilder().handlerClass(actionProcessorClass).build())
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
            log.info("Processing action types of " + type);
            List<SecondaryTermLoadAction> transactionActions = groupedByType.get(type);
            processLoadActions(type, transactionActions);
            log.info("Finished action types of " + type);
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
        log.info("Committing changes");
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
        Class handlerClass = subTypeActions.get(0).getHandlerClass();
        ActionProcessor handler = getHandler(handlerClass);
        if (handler.isSubTypeHandlerFor() != subType) {
            throw new RuntimeException("Handler class " + handlerClass + " does not support subType " + subType);
        }
        log.info("Processing " + subTypeActions.size() + " actions for " + subType + " using handler " + handlerClass);
        Date timestamp = new Date();
        handler.processActions(subTypeActions);
        long timeElapsed = new Date().getTime() - timestamp.getTime();
        long secondsElapsed = timeElapsed / 1000;
        log.info("Finished processing " + subTypeActions.size() + " actions in " + secondsElapsed + " seconds for " + subType + " using handler " + handlerClass);
    }

    /**
     * Use reflection to get an instance of the class that handles the actions.
     * @param handlerClass the class name
     * @return the handler
     */
    private static ActionProcessor getHandler(Class<? extends ActionProcessor> handlerClass) {
        try {
            return handlerClass.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            // Handle other potential exceptions
            e.printStackTrace();
            throw new RuntimeException("Could not instantiate handler class: " + handlerClass);
        }
    }

}
