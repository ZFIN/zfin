package org.zfin.framework.featureflag;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "zdb_feature_flag")
@Setter
@Getter
@JsonView(View.API.class)
public class FeatureFlag {
    @Id
    @Column(name = "zfeatflag_name", unique = true, nullable = false)
    private String name;

    @Column(name = "zfeatflag_enabled")
    private boolean enabledForGlobalScope;

    @Column(name = "zfeatflag_last_modified")
    private Date lastModified;

    @Transient
    private boolean enabledForPersonScope;

    @Transient
    private boolean enabled;

    public boolean isEnabled() {
        return FeatureFlags.isFlagEnabled(this);
    }

    public boolean isEnabledForPersonScope() {
        return FeatureFlags.isFlagEnabledForPersonScope(this).equals(FeatureFlags.FlagState.ENABLED);
    }

}
