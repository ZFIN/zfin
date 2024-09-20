package org.zfin.infrastructure;

import java.util.Date;
import java.util.List;

import org.zfin.profile.HasImage;

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
                .map(u -> updateToDTO(u))
                .toList();
    }

    private static UpdatesDTO updateToDTO(Updates update) {
        String name = update.getSubmitter() != null ? update.getSubmitter().getDisplay() : update.getSubmitterName();
        String image = update.getSubmitter() != null ? update.getSubmitter().getImage() : null;
        String zdbID = update.getSubmitter() != null ? update.getSubmitter().getZdbID() : null;
        UpdatesDTO dto = new UpdatesDTO(
                name,
                image,
                update.getFieldName(),
                update.getOldValue(),
                update.getNewValue(),
                update.getComments(),
                update.getWhenUpdated(),
                zdbID
        );
        return dto;
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