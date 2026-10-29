package org.zfin.sequence.gff;

import jakarta.persistence.Tuple;
import org.hibernate.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.framework.HibernateUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Developer page for marking assemblies as current or not. More than one assembly can be current at once.
 * "Current" is read live by the marker_annotation_status_derived view, so a change shows up in the
 * annotation status as soon as it is saved.
 */
@Controller
public class AssemblyAdminController {

    @RequestMapping("/devtool/assemblies")
    public String page() {
        return "dev-tools/assemblies";
    }

    @RequestMapping(value = "/devtool/assemblies/data", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> data() {
        List<Map<String, Object>> assemblies = new ArrayList<>();
        for (Tuple row : HibernateUtil.currentSession().createNativeQuery(
                "select a_pk_id, a_name, a_gcf_identifier, a_order, a_is_current from assembly order by a_order", Tuple.class)
                .getResultList()) {
            Map<String, Object> assembly = new LinkedHashMap<>();
            assembly.put("id", row.get("a_pk_id", Number.class).longValue());
            assembly.put("name", row.get("a_name", String.class));
            assembly.put("gcfIdentifier", row.get("a_gcf_identifier", String.class));
            assembly.put("order", row.get("a_order", Number.class).intValue());
            assembly.put("current", row.get("a_is_current", Boolean.class));
            assemblies.add(assembly);
        }
        return assemblies;
    }

    public record CurrentChange(long id, boolean current) {
    }

    @RequestMapping(value = "/devtool/assemblies/current", method = RequestMethod.POST)
    @ResponseBody
    public List<Map<String, Object>> setCurrent(@RequestBody CurrentChange change) {
        HibernateUtil.createTransaction();
        Session session = HibernateUtil.currentSession();
        session.createNativeQuery("update assembly set a_is_current = :current where a_pk_id = :id")
                .setParameter("current", change.current()).setParameter("id", change.id()).executeUpdate();
        HibernateUtil.flushAndCommitCurrentSession();
        return data();
    }
}
