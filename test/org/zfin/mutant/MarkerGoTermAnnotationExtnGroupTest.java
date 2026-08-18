package org.zfin.mutant;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * equals/hashCode contract for MarkerGoTermAnnotationExtnGroup.
 *
 * equals() used to return an unconditional true with no hashCode() override, so any two groups
 * compared equal. The missing hashCode masked it: instances fell back on identity hash codes and
 * usually landed in different buckets of the Set<MarkerGoTermAnnotationExtnGroup> on
 * MarkerGoTermEvidence, so equals was never consulted. On a bucket collision the set silently
 * dropped the second group -- and collisions are not rare once an annotation carries many groups.
 */
public class MarkerGoTermAnnotationExtnGroupTest {

    private MarkerGoTermAnnotationExtnGroup group(Long id) {
        MarkerGoTermAnnotationExtnGroup group = new MarkerGoTermAnnotationExtnGroup();
        group.setId(id);
        return group;
    }

    /** The bug: two unrelated groups were equal, so a set could only ever hold one of them. */
    @Test
    public void distinctPersistedGroupsAreNotEqual() {
        assertNotEquals(group(1L), group(2L));
    }

    @Test
    public void sameIdIsEqualAndAgreesOnHashCode() {
        MarkerGoTermAnnotationExtnGroup a = group(7L);
        MarkerGoTermAnnotationExtnGroup b = group(7L);

        assertEquals(a, b);
        assertEquals("equal objects must agree on hashCode", a.hashCode(), b.hashCode());
    }

    /**
     * A group with no id has not been persisted yet, so nothing identifies it but itself.
     * Two transient groups must stay distinct even when they would carry the same contents --
     * otherwise a set silently discards one before either gets an id.
     */
    @Test
    public void transientGroupsAreEqualOnlyToThemselves() {
        MarkerGoTermAnnotationExtnGroup a = group(null);
        MarkerGoTermAnnotationExtnGroup b = group(null);

        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, group(3L));
        assertNotEquals(group(3L), a);
    }

    /**
     * hashCode must not change when the database assigns the id, or a group already in a set
     * becomes unfindable in it. This is why hashCode is a class constant rather than id-derived.
     */
    @Test
    public void hashCodeSurvivesBecomingPersistent() {
        MarkerGoTermAnnotationExtnGroup group = group(null);
        Set<MarkerGoTermAnnotationExtnGroup> set = new HashSet<>();
        set.add(group);

        int before = group.hashCode();
        group.setId(42L);

        assertEquals(before, group.hashCode());
        assertTrue("group must still be findable after the id is assigned", set.contains(group));
    }

    /** What the broken equals actually cost: distinct groups collapsing to one set element. */
    @Test
    public void aSetRetainsEveryDistinctGroup() {
        Set<MarkerGoTermAnnotationExtnGroup> set = new HashSet<>();
        set.add(group(1L));
        set.add(group(2L));
        set.add(group(3L));
        set.add(group(3L));     // duplicate of an existing id -- genuinely the same group
        set.add(group(null));
        set.add(group(null));   // two separate unpersisted groups

        assertEquals(5, set.size());
    }

    @Test
    public void neverEqualToNullOrAnotherType() {
        assertNotEquals(group(1L), null);
        assertNotEquals(group(1L), "ZDB-MRKRGOEV-260812-62218");
    }
}
