package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Generates {@code home/javascript/react/zirc/api/types.ts} from the
 * Java DTO records in {@code org.zfin.zirc.dto}. Removes the per-field
 * Java↔TS mirror that was the largest pure-redundancy in the form
 * pipeline (see {@code reference/zirc-architecture.md} §3).
 *
 * <p>Invoked by the Gradle {@code generateZircTypes} task. CI runs it
 * with {@code --check} (or via a comparison step) to fail when the
 * committed file drifts from what the DTOs declare.
 *
 * <p>Type mapping (the subset our DTOs use):
 * <ul>
 *   <li>{@code String}, {@code char} → {@code string}</li>
 *   <li>{@code Long}/{@code Integer}/{@code Double}/{@code int}/{@code long}/{@code double} → {@code number}</li>
 *   <li>{@code Boolean}/{@code boolean} → {@code boolean}</li>
 *   <li>{@code Instant}/{@code LocalDate}/{@code LocalDateTime} → {@code string} (ISO-8601)</li>
 *   <li>{@code String[]} → {@code string[]}</li>
 *   <li>{@code List<T>} → {@code T[]}</li>
 *   <li>Nested DTO records → reference to the named interface</li>
 * </ul>
 *
 * <p>Boxed components default to nullable on the wire (Jackson emits
 * {@code null}); only primitive components and those marked
 * {@link NotNull} are emitted as non-nullable. {@code @NotNull} is the
 * server-side promise that the field is always populated on the
 * outgoing JSON — used on IDs, foreign-key IDs, and sort-order columns
 * that are server-set and never null for an existing record.
 */
public final class GenerateTypeScript {

    /** Order matters: dependent types after their dependencies isn't
     *  strictly required (TS hoists), but a deterministic order keeps
     *  the diff readable when fields move around. */
    private static final List<Class<?>> DTO_CLASSES = List.of(
            LineSubmissionDTO.class,
            MutationDTO.class,
            LinkedFeatureDTO.class,
            LesionSummaryDTO.class,
            LesionDTO.class,
            GeneDTO.class,
            AssaySummaryDTO.class,
            AssayDTO.class,
            AssayFileDTO.class,
            PhenotypeSummaryDTO.class,
            PhenotypeDTO.class,
            AutocompleteItemDTO.class);

    /** Extra interfaces emitted by hand — types the React client uses
     *  that don't have a matching Java DTO (the RFC 7807 problem
     *  detail is parsed off our exception advice, not declared as a
     *  Jackson DTO). */
    private static final String EXTRAS = """
            // RFC 7807 problem detail returned by ZircApiExceptionHandler.
            export interface ProblemDetail {
                type?: string;
                title?: string;
                status?: number;
                detail?: string;
                instance?: string;
                errors?: Record<string, string>;
            }
            """;

    public static void main(String[] args) throws IOException {
        Path out = args.length > 0
                ? Path.of(args[0])
                : Path.of("home/javascript/react/zirc/api/types.ts");

        String generated = render();
        Files.writeString(out, generated, StandardCharsets.UTF_8);
        System.out.println("Wrote " + out.toAbsolutePath());
    }

    static String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                // GENERATED FILE — do not edit.
                //
                // Source: org.zfin.zirc.dto records. Regenerate via
                //   gradle generateZircTypes
                //
                // See reference/zirc-architecture.md §3 for why this
                // mirror exists and what the generator covers.

                """);

        for (Class<?> dto : DTO_CLASSES) {
            sb.append(renderInterface(dto));
            sb.append('\n');
        }

        sb.append(EXTRAS);
        return sb.toString();
    }

    private static String renderInterface(Class<?> dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("export interface ").append(dto.getSimpleName()).append(" {\n");
        for (RecordComponent c : dto.getRecordComponents()) {
            String tsType = tsType(c.getGenericType());
            boolean nullable = isBoxed(c.getType()) && !isMarkedNotNull(c);
            sb.append("    ")
                    .append(c.getName())
                    .append(": ")
                    .append(tsType)
                    .append(nullable ? " | null" : "")
                    .append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    /** Server-side {@code @NotNull} promise that the field is always
     *  populated on the wire. Used by IDs and other server-set columns.
     *
     *  <p>Goes through the synthetic record field because Jakarta's
     *  {@code @NotNull} target set doesn't include
     *  {@code RECORD_COMPONENT}; the annotation is propagated to the
     *  field at compile time but not visible on {@link RecordComponent}
     *  directly. */
    private static boolean isMarkedNotNull(RecordComponent c) {
        try {
            return c.getDeclaringRecord()
                    .getDeclaredField(c.getName())
                    .isAnnotationPresent(NotNull.class);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private static String tsType(Type t) {
        if (t instanceof Class<?> c) {
            if (c.isArray()) {
                return tsType(c.getComponentType()) + "[]";
            }
            return tsScalar(c);
        }
        if (t instanceof ParameterizedType pt && pt.getRawType() instanceof Class<?> raw) {
            if (List.class.isAssignableFrom(raw)) {
                Type elementType = pt.getActualTypeArguments()[0];
                return tsType(elementType) + "[]";
            }
        }
        // Open Jackson types — JsonSchema and UiSchemaElement on
        // FormSchemaDTO. The client doesn't traverse them by hand;
        // it hands them straight to JSON Forms.
        return "unknown";
    }

    private static String tsScalar(Class<?> c) {
        if (c == String.class || c == char.class || c == Character.class) {return "string";}
        if (c == boolean.class || c == Boolean.class) {return "boolean";}
        if (c == int.class || c == Integer.class
                || c == long.class || c == Long.class
                || c == double.class || c == Double.class
                || c == float.class || c == Float.class
                || c == short.class || c == Short.class
                || c == byte.class || c == Byte.class) {
            return "number";
        }
        if (c == Instant.class || c == LocalDate.class || c == LocalDateTime.class) {
            return "string";
        }
        if (DTO_CLASSES.contains(c)) {
            return c.getSimpleName();
        }
        // Unknown class — emit a clear marker so a reviewer notices.
        return "unknown /* " + c.getSimpleName() + " */";
    }

    /** Boxed components produce nullable TS unions; primitives don't. */
    private static boolean isBoxed(Class<?> type) {
        if (type.isPrimitive()) {return false;}
        if (type.isArray()) {return false;}
        if (List.class.isAssignableFrom(type)) {return false;}
        return true;
    }

    /** Suppress accidental instantiation; this class is `main`-only. */
    private GenerateTypeScript() {}
}
