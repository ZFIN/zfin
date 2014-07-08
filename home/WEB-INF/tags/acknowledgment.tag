<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="publication" type="org.zfin.publication.Publication"%>
<%@attribute name="showElsevierMessage" type="java.lang.Boolean" rtexprvalue="true" required="true" %>
<%@attribute name="hasAcknowledgment" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<%--
 Doc from app page:

 Display pub acknowledgments based on figure curation:
  FULL: Includes the journal acknowledgment and pub specific acknowledgment.
  LIGHT: Display notice of copywrite restrictions.
  GELI/None: Display nothing.

INPUT VARS:

  REQUIRED:
    $pubackn_pub_zdb_id :: publication zdb-id

  OPTIONAL:
    $pubackn_display :: full table or text only selection; defaults to full table

OUTPUT VARS: none

OUTPUT:
  Displays acknowledgments in a fomatted table.



      <?MIVAR COND=$(NC,$pubackn_journal_ack,)>$pubackn_journal_ack<?/MIVAR>
      <?MIVAR COND=$(NC,$pubackn_pub_ack,)><br><br>$pubackn_pub_ack<?/MIVAR>
      <?MIVAR COND=$(EC,$pubackn_jrnl_publisher,Elsevier)><br><br>Reprinted from
         $pubackn_jrnl_name,
         $pubackn_pub_volume, $pubackn_pub_authors, $pubackn_pub_name, $pubackn_pub_pages, Copyright
         ($pubackn_copyrt_year) with permission from Elsevier.
      <?/MIVAR>

EFFECTS

--%>


<zfin2:subsection>

    <table class="summary">
        <tr>
            <th>Acknowledgments:</th>
        </tr>
        <td>

            ZFIN wishes to thank the journal ${publication.journal.name} for permission to reproduce figures from this article.
            Please note that this material may be protected by copyright.

            <zfin-figure:additionalAcknowledgment publication="${publication}" hasAcknowledgment="${hasAcknowledgment}" />

            <zfin-figure:elsevierMessage publication="${publication}" showElsevierMessage="${showElsevierMessage}" />

            <zfin-figure:journalAbbrev publication="${publication}"/>
        </td>

    </table>




</zfin2:subsection>