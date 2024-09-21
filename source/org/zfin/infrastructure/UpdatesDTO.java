package org.zfin.infrastructure;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.profile.HasImage;
import org.zfin.profile.Person;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.PublicationTrackingStatus;

import java.util.*;
import java.util.stream.Collectors;

public record UpdatesDTO(
        @JsonView(View.API.class)
        Long id,

        @JsonView(View.API.class)
        String submitterName,

        @JsonView(View.API.class)
        String submitterImage,

        @JsonView(View.API.class)
        String fieldName,

        @JsonView(View.API.class)
        String oldValue,

        @JsonView(View.API.class)
        String newValue,

        @JsonView(View.API.class)
        String comments,

        @JsonView(View.API.class)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        Date whenUpdated,

        @JsonView(View.API.class)
        String submitterZdbID
) implements HasImage {

    @JsonView(View.API.class)
    @JsonProperty("uniqueKey")
    public String uniqueKey() {
        return id + fieldName + whenUpdated;
    }


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
                update.getID(),
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
                publicationEvent.getId(),
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