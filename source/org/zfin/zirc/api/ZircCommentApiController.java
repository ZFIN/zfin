package org.zfin.zirc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.zirc.dto.LineSubmissionCommentDTO;
import org.zfin.zirc.entity.LineSubmissionComment;
import org.zfin.zirc.service.ZircCommentService;

import java.util.List;

/**
 * REST surface for per-field / per-section comments. Two endpoints:
 *
 *   GET  /api/zirc/comments?recId=X&scope=field&fieldName=Y
 *   GET  /api/zirc/comments?recId=X&scope=section&sectionName=Y
 *   POST /api/zirc/comments  body {recId, scope, fieldName|sectionName, comment}
 *
 * <p>Author is the current authenticated user; the client never sends it.
 */
@RestController
@RequestMapping(path = "/api/zirc", produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircCommentApiController {

    @Autowired
    private ZircCommentService commentService;

    @GetMapping("/comments")
    public List<LineSubmissionCommentDTO> list(
            @RequestParam String recId,
            @RequestParam String scope,
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) String sectionName) {

        List<LineSubmissionComment> rows;
        if ("field".equals(scope)) {
            if (fieldName == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "fieldName is required for scope='field'");
            }
            rows = commentService.listField(recId, fieldName);
        } else if ("section".equals(scope)) {
            if (sectionName == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "sectionName is required for scope='section'");
            }
            rows = commentService.listSection(recId, sectionName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "scope must be 'field' or 'section'");
        }
        return enrich(rows);
    }

    public record CreateRequest(
            String recId,
            String scope,
            String fieldName,
            String sectionName,
            String comment,
            boolean closed) {}

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public LineSubmissionCommentDTO create(@RequestBody CreateRequest req) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authenticated user required to post a comment");
        }
        LineSubmissionComment saved = commentService.add(
                req.recId(), req.scope(), req.fieldName(), req.sectionName(),
                currentUser, req.comment(), req.closed());
        return LineSubmissionCommentDTO.of(saved, currentUser);
    }

    /**
     * Build DTOs from rows. The author relationship is loaded lazily per
     * row; if the per-page comment count grows, add JOIN FETCH on author
     * to the list queries in {@link ZircCommentService}.
     */
    private List<LineSubmissionCommentDTO> enrich(List<LineSubmissionComment> rows) {
        if (rows.isEmpty()) {return List.of();}
        return rows.stream()
                .map(c -> LineSubmissionCommentDTO.of(c, c.getAuthor()))
                .toList();
    }
}
