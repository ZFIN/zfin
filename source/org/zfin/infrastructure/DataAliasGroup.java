package org.zfin.infrastructure;

import javax.persistence.*;
import java.io.Serializable;

/**
 * This class is only used for validation of the correct
 * group enumeration items.
 * Use DataAlias.Group enum if needed.
 */
@Entity
@Table(name = "alias_group")
public class DataAliasGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aliasgrp_pk_id")
    private int id;
    @Column(name = "aliasgrp_name")
    private String name;
    @Column(name = "aliasgrp_significance")
    private int significance;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSignificance() {
        return significance;
    }

    public void setSignificance(int significance) {
        this.significance = significance;
    }

    public enum Group implements Serializable {
        ALIAS("alias"),
        PLURAL("plural"),
        SECONDARY_ID("secondary id"),
        SEQUENCE_SIMILARITY("sequence similarity"),
        SYSTEMATIC_SYNONYM("systematic_synonym"),
        VERTEBRATE("vertebrate");

        private String value;

        private Group(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }


}
