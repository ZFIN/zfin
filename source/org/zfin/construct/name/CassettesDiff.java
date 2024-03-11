 package org.zfin.construct.name;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class CassettesDiff {
    private List<String> promoterMarkersAdded = new ArrayList<>();
    private List<String> promoterMarkersRemoved = new ArrayList<>();
    private List<String> codingMarkersAdded = new ArrayList<>();
    private List<String> codingMarkersRemoved = new ArrayList<>();

    public static CassettesDiff calculate(Cassettes before, Cassettes after) {
        List<String> beforePromoterParts = new ArrayList<>();
        before.getCassettes().forEach(cassette -> {
            beforePromoterParts.addAll(cassette.getPromoter().getPromoter());
        });

        List<String> afterPromoterParts = new ArrayList<>();
        after.getCassettes().forEach(cassette -> {
            afterPromoterParts.addAll(cassette.getPromoter().getPromoter());
        });

        CassettesDiff diff = new CassettesDiff();
        CollectionUtils.subtract(afterPromoterParts, beforePromoterParts).forEach(diff.getPromoterMarkersAdded()::add);
        CollectionUtils.subtract(beforePromoterParts, afterPromoterParts).forEach(diff.getPromoterMarkersRemoved()::add);

        List<String> beforeCodingParts = new ArrayList<>();
        before.getCassettes().forEach(cassette -> {
            beforeCodingParts.addAll(cassette.getCoding().getCoding());
        });

        List<String> afterCodingParts = new ArrayList<>();
        after.getCassettes().forEach(cassette -> {
            afterCodingParts.addAll(cassette.getCoding().getCoding());
        });

        CollectionUtils.subtract(afterCodingParts, beforeCodingParts).forEach(diff.getCodingMarkersAdded()::add);
        CollectionUtils.subtract(beforeCodingParts, afterCodingParts).forEach(diff.getCodingMarkersRemoved()::add);
        return diff;
    }
}
