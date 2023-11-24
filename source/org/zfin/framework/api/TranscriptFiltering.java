package org.zfin.framework.api;

import org.zfin.marker.Transcript;

public class TranscriptFiltering extends Filtering<Transcript> {


    public TranscriptFiltering() {
        filterFieldMap.put(FieldFilter.TYPE, typeFilter);
        filterFieldMap.put(FieldFilter.STATUS, statusFilter);
        filterFieldMap.put(FieldFilter.TRANSCRIPT_ID, idFilter);
        filterFieldMap.put(FieldFilter.STATUS_EMPTY, emptyStatusFilter);
    }

    public static FilterFunction<Transcript, String> idFilter =
        (transcript, value) -> FilterFunction.contains(transcript.getZdbID(), value);

    public static FilterFunction<Transcript, String> typeFilter =
        (transcript, value) -> FilterFunction.contains(transcript.getTranscriptType().getType().toString(), value);

    public static FilterFunction<Transcript, String> statusFilter =
        (transcript, value) -> {
            if (transcript.getStatus() != null) {
                return FilterFunction.contains(transcript.getStatus().getStatus().toString(), value);
            }
            return false;
        };

    public static FilterFunction<Transcript, String> emptyStatusFilter =
        (transcript, value) -> transcript.getStatus() == null;

}
