package org.zfin.feature;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "feature_tracking")
public class FeatureTracking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ft_pk_id")
  private int pkid;

  @ManyToOne
  @JoinColumn(name = "ft_feature_zdb_id")
  private Feature feature;

  @Column(name = "ft_feature_abbrev")
  private String featTrackingFeatAbbrev;
}
