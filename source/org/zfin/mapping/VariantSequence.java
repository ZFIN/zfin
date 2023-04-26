package org.zfin.mapping;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VariantSequence  {
    private String zdbID;
    private String vseqDataZDB;
    private String vfsTargetSequence;
    private int vfsOffsetStart;
    private int vfsOffsetStop;
    private String vfsVariation;
    private String vfsLeftEnd;
    private String vfsRightEnd;
    private String vfsType;
    private String vfsFlankType;
    private String vfsFlankOrigin;

}
