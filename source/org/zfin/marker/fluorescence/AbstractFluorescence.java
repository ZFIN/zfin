
package org.zfin.marker.fluorescence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.util.FluorescenceUtil;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
public abstract class AbstractFluorescence {

    public abstract Integer getEmissionLength();

    public abstract Integer getExcitationLength();

    public abstract String getEmissionColor();

    public abstract String getExcitationColor();

    public String getEmissionColorHex() {
        if (getEmissionLength() != null) {
            return FluorescenceUtil.waveLengthToHex(getEmissionLength());
        }
        return null;
    }

    @JsonView(View.API.class)
    @JsonProperty("emissionColorHex")
    public String getEmissionColorHexFixed() {
        if (getEmissionLength() != null) {
            return FluorescenceUtil.waveLengthToHexFixed(getEmissionColor());
        }
        return null;
    }

    public String getExcitationColorHex() {
        if (getExcitationLength() != null) {
            return FluorescenceUtil.waveLengthToHex(getExcitationLength());
        }
        return null;
    }

    @JsonView(View.API.class)
    @JsonProperty("excitationColorHex")
    public String getExcitationColorHexFixed() {
        if (getExcitationLength() != null) {
            return FluorescenceUtil.waveLengthToHexFixed(getExcitationColor());
        }
        return null;
    }

}
