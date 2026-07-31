package org.zfin.zirc.dto;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * CI guardrail for the generated ZIRC TypeScript DTO mirror.
 *
 * <p>Runs {@link GenerateTypeScript#render()} in-process and asserts the
 * output is byte-equal to the committed
 * {@code frontend/javascript/react/zirc/api/types.ts}. Forgetting to rerun
 * {@code gradle generateZircTypes} after changing a DTO would otherwise
 * silently de-sync the React client from the wire shape; this test
 * fails the build at that point with a single, obvious next step.
 */
public class GenerateTypeScriptDriftTest {

    @Test
    public void committedTypesFileMatchesGeneratorOutput() throws Exception {
        Path expected = locateTypesFile();
        if (!Files.exists(expected)) {
            fail("Could not locate committed types.ts at " + expected.toAbsolutePath()
                    + ". Working directory: " + new File(".").getAbsolutePath());
        }
        String committed = Files.readString(expected, StandardCharsets.UTF_8);
        String generated = GenerateTypeScript.render();

        assertEquals(
                "frontend/javascript/react/zirc/api/types.ts is out of sync with the Java DTOs. "
                        + "Rerun `gradle generateZircTypes` and commit the result. "
                        + "See reference/zirc-architecture.md §3.",
                generated,
                committed);
    }

    /**
     * Same probe pattern as {@link org.zfin.zirc.api.FormSchemaSnapshotTest}
     * — the working directory differs depending on how the test is
     * invoked. Probe both common roots.
     */
    private static Path locateTypesFile() {
        String rel = "frontend/javascript/react/zirc/api/types.ts";
        File here = new File(rel);
        File parent = new File("../" + rel);
        return (here.exists() || here.getParentFile().exists())
                ? here.toPath()
                : parent.toPath();
    }
}
