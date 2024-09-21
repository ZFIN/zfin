package org.zfin.marker.fluorescence;

import java.util.List;

public record FPBaseApiResultItem(
    String uuid,
    String name,
    String slug,
    String seq,
    String ipg_id,
    String genbank,
    String uniprot,
    List<String> pdb,
    String agg,
    String switch_type,
    List<FPBaseApiResultState> states,
    List<FPBaseApiResultTransition> transitions,
    String doi
) {}
