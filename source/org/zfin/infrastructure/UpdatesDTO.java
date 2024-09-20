package org.zfin.infrastructure;

import org.zfin.profile.HasImage;
import org.zfin.profile.Person;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.PublicationTrackingStatus;

import java.util.*;
import java.util.stream.Collectors;

public record UpdatesDTO(
        String submitterName,
        String submitterImage,
        String fieldName,
        String oldValue,
        String newValue,
        String comments,
        Date whenUpdated,
        String submitterZdbID
) implements HasImage {


    public static List<UpdatesDTO> fromUpdates(List<Updates> updates) {
        return updates.stream()
                .map(UpdatesDTO::updateToDTO)
                .collect(Collectors.toList());
    }

    private static UpdatesDTO updateToDTO(Updates update) {
        String name = update.getSubmitter() != null ? update.getSubmitter().getDisplay() : update.getSubmitterName();
        String image = update.getSubmitter() != null ? update.getSubmitter().getImage() : null;
        String zdbID = update.getSubmitter() != null ? update.getSubmitter().getZdbID() : null;
        return new UpdatesDTO(
                name,
                image,
                update.getFieldName(),
                update.getOldValue(),
                update.getNewValue(),
                update.getComments(),
                update.getWhenUpdated(),
                zdbID
        );
    }

    public static List<UpdatesDTO> fromPublicationEvents(List<PublicationTrackingHistory> events) {
        List<UpdatesDTO> updates = new ArrayList<>();
        UpdatesDTO previous = null;

        List<PublicationTrackingHistory> orderedEvents = events.stream().sorted(Comparator.comparing(PublicationTrackingHistory::getDate)).toList();

        for(PublicationTrackingHistory event : orderedEvents) {
            UpdatesDTO tempEvent = publicationEventToDTO(event, previous);
            updates.add(tempEvent);
            previous = tempEvent;
        }
        updates.sort(Comparator.comparing(UpdatesDTO::getWhenUpdated));
        for(UpdatesDTO update : updates) {
            System.out.println(update.getWhenUpdated());
        }

        return updates;
    }

    private static UpdatesDTO publicationEventToDTO(PublicationTrackingHistory publicationEvent, UpdatesDTO previous) {
        PublicationTrackingStatus.Name statusName = publicationEvent.getStatus().getName();
        Calendar date = publicationEvent.getDate();
        Person performedBy = publicationEvent.getPerformedBy();

        String submitterName = performedBy != null ? performedBy.getDisplay() : null;
        String image = performedBy != null ? performedBy.getImage() : null;
        String fieldName = "Status";
        String oldValue = previous != null ? previous.getNewValue() : null;
        String newValue = statusName.toString();
        String display = publicationEvent.getDisplay();
        Date updated = date.getTime();
        String submitterZdbID = performedBy != null ? performedBy.getZdbID() : null;

        return new UpdatesDTO(
                submitterName,
                image,
                fieldName,
                oldValue,
                newValue,
                display,
                updated,
                submitterZdbID
        );
    }

    //getters for jsp access
    public String getSubmitterName() {
        return submitterName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getComments() {
        return comments;
    }

    public Date getWhenUpdated() {
        return whenUpdated;
    }

    //implement interface for HasImage
    @Override
    public String getZdbID() {
        return submitterZdbID;
    }

    @Override
    public String getImage() {
        return submitterImage;
    }

    @Override
    public void setImage(String image) {
        // do nothing
    }
}