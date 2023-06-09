package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.zfin.framework.api.View;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

@Data
public class MarkerHistoryReasonsDTO implements Serializable {

    @JsonView(View.API.class)
    private List<String> reasons;

    public static MarkerHistoryReasonsDTO allReasons() {
        MarkerHistoryReasonsDTO dto = new MarkerHistoryReasonsDTO();
        MarkerHistory.Reason[] values = MarkerHistory.Reason.values();
        dto.setReasons(List.of(values).stream().map(MarkerHistory.Reason::toString).toList());
        return dto;
    }

}
