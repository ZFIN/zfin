package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.zfin.framework.api.View;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

@Data
public class MarkerHistoryDTO implements Serializable {

    @JsonView(View.API.class)
    private String eventName;
    @JsonView(View.API.class)
    private String eventDisplay;
    @JsonView(View.API.class)
    private String zdbID;
    @JsonView(View.API.class)
    private String reason;
    @JsonView(View.API.class)
    private String newValue;
    @JsonView(View.API.class)
    private String oldSymbol;
    @JsonView(View.API.class)
    private String date;
    @JsonView(View.API.class)
    private String time;
    @JsonView(View.API.class)
    private String comments;
    @JsonView(View.API.class)
    private List<String> attributions;

    @JsonView(View.API.class)
    private int attributionsSize;
    @JsonView(View.API.class)
    private String firstPublication;

    public static MarkerHistoryDTO fromMarkerHistory(MarkerHistory history) {
        MarkerHistoryDTO dto = new MarkerHistoryDTO();
        dto.setEventName(history.getEvent().toString());
        dto.setEventDisplay(history.getEvent().getDisplay());
        dto.setZdbID(history.getZdbID());
        if (history.getReason() != null) {
            dto.setReason(history.getReason().toString());
        }
        dto.setNewValue(history.getNewValue());
        dto.setOldSymbol(history.getOldSymbol());
        dto.setDate(new SimpleDateFormat("yyyy-MM-dd").format(history.getDate()));
        dto.setTime(new SimpleDateFormat("HH:mm:ss").format(history.getDate()));
        dto.setComments(history.getComments());
        dto.setAttributionsSize(0);
        dto.setAttributions(history.getAttributions().stream().map(p -> p.getPublication().getZdbID()).toList());
        if (history.getAttributions() != null) {
            dto.setAttributionsSize(history.getAttributions().size());
            if (history.getAttributions().size() > 0) {
                String firstAttribution = history.getAttributions().iterator().next().getPublication().getZdbID();
                dto.setFirstPublication(firstAttribution);
            }
        }
        return dto;
    }

}
