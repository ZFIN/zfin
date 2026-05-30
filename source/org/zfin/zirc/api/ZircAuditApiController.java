package org.zfin.zirc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.zirc.dto.AuditEntryDTO;
import org.zfin.zirc.entity.AuditEntry;
import org.zfin.zirc.service.ZircAuditQueryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Audit-log read surface for the FieldHistory popup. One endpoint, two
 * shapes:
 *
 *   GET /api/zirc/audit?recId=...&scope=field&fieldName=...
 *   GET /api/zirc/audit?recId=...&scope=section&sectionName=...
 *
 * <p>recId conventions match what the React detail page emits via config
 * (submission ZDB-ID for the top level; {@code ZIRC-<KIND>-<id>} for
 * nested aggregates). Author is resolved via a bulk-by-zdbID person fetch
 * so the popup never N+1s.
 */
@RestController
@RequestMapping(path = "/api/zirc", produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircAuditApiController {

    @Autowired
    private ZircAuditQueryService auditService;

    @GetMapping("/audit")
    public List<AuditEntryDTO> list(
            @RequestParam String recId,
            @RequestParam String scope,
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) String sectionName) {

        List<AuditEntry> rows;
        if ("field".equals(scope)) {
            if (fieldName == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "fieldName is required for scope='field'");
            }
            rows = auditService.listField(recId, fieldName);
        } else if ("section".equals(scope)) {
            if (sectionName == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "sectionName is required for scope='section'");
            }
            rows = auditService.listSection(recId, sectionName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "scope must be 'field' or 'section'");
        }
        if (rows.isEmpty()) return List.of();

        List<String> actorIds = rows.stream().map(AuditEntry::getActor).distinct().toList();
        List<Person> people = HibernateUtil.currentSession()
                .createQuery("from Person where zdbID in :ids", Person.class)
                .setParameterList("ids", actorIds)
                .list();
        Map<String, Person> byId = new HashMap<>();
        for (Person p : people) byId.put(p.getZdbID(), p);

        List<AuditEntryDTO> out = new ArrayList<>(rows.size());
        for (AuditEntry a : rows) {
            out.add(AuditEntryDTO.of(a, byId.get(a.getActor())));
        }
        return out;
    }
}
