<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequence" type="java.lang.String" rtexprvalue="true" required="true" %>
<div class='btn-group'>
    <button
       class='btn btn-outline-secondary btn-sm dropdown-toggle'
       data-toggle='dropdown'
       aria-haspopup='true'
       aria-expanded='false'
    >
        Select Tool
    </button>
    <div class='dropdown-menu'>



        
                        <a class='dropdown-item'
                           href=""${ncbiBlastUrl}${sequence}">NCBI BLAST</a>
                    
                    
                        <a class='dropdown-item'
                           href="action/blast/blast?&program=blastn&sequenceType=nt&queryType=FASTA&shortAndNearlyExact=true&expectValue=1e-10&dataLibraryString=RNASequences&querySequence=${sequence}">ZFIN
                            BLAST</a>
                    
        
    </div>
</div>
