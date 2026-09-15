package org.zfin.zirc.api;

import org.zfin.zirc.api.jsonschema.ArraySchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.NumberSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.api.jsonschema.StringSchema;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The JSON Schema for an {@code attachments} array — the summary rows the
 * {@code attachmentsList} widget renders, matching {@code AssayFileDTO} and
 * {@code PhenotypeFileDTO}.
 *
 * <p>Extracted when phenotypes gained uploads (ZFIN-10449), so a second owner
 * did not mean a second hand-maintained copy of the item shape. The per-owner
 * cap is a parameter because it is genuinely per-owner; everything else about
 * an attachment row is not.
 *
 * <p>{@code maxItems} is what publishes the cap to the client: the widget
 * disables its file input once the list reaches it, so the server-side limit
 * and the disabled state cannot disagree.
 *
 * <p>{@code ZircAssayFormSchema} still carries its own private equivalent.
 * Folding it in here is a two-line change, deliberately not made on this
 * branch: that file is heavily edited on the parallel ZIRC form branches and
 * the conflict would cost more than the duplication. Worth doing once those
 * land.
 */
public final class ZircAttachmentSchema {

    private ZircAttachmentSchema() {}

    public static ArraySchema attachmentsArrayProp(String title, int maxItems) {
        Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
        itemProps.put("id",               NumberSchema.of());
        itemProps.put("originalFilename", new StringSchema(null, null, null, null, null));
        itemProps.put("contentType",      StringSchema.nullable());
        itemProps.put("fileSize",         new NumberSchema(null, Boolean.TRUE));
        itemProps.put("uploadedAt",       StringSchema.nullable());
        return new ArraySchema(title, ObjectSchema.of(itemProps), maxItems, null);
    }
}
