
<div class="popup-header">
    Explanation of Relationship Types
</div>
<div class="popup-body">
    Relationships and their reciprocal types are shown between terms of the same ontology.
    <br/>
    The two major relationship types used in all ontologies are:
    <p></p>

    <table class="summary groupstripes">
        <tr class="odd  ">
            <th>
                <nobr>is part of <br/> has part</nobr>
            </th>
            <td> Term B is a part of term A during the time at which they both exist.
                Term A may have a part B during the time at which they both exist.
                <ul>
                    <li>leaf <b>is part of</b>: tree</li>
                    <li>tree <b>has part</b>: leaf, trunk, root</li>
                </ul>
            </td>
        </tr>
        <tr class="odd newgroup ">
            <th>
                <nobr>has type <br/> is a type of</nobr>
            </th>
            <td> Term B is a type of term A. Term A has subtypes B and C.
                <ul>
                    <li>parrot <b>is a type of</b>: bird</li>
                    <li>bird <b>has type</b>: parrot, eagle, pigeon</li>
                </ul>
            </td>
        </tr>
    </table>
    <p/>
    The anatomical ontology (AO) uses another relationship type indicating how structures develop from another one in time:

    <table class="summary groupstripes">
        <tr class="odd newgroup">
            <th>
                <nobr>develops from <br/> develops into</nobr>
            </th>
            <td> Structure A develops from structure B. Structure B develops into structure A. There
                can be a one to one, one to many, or many to one correspondence. For example, structure A develops from
                structure B and C, or structure A develops into structure B and C.
                <ul>
                    <li>cheese <b>develops from</b>: milk, bacteria</li>
                    <li>milk <b>develops into</b>: cheese, yogurt, ice cream</li>
                </ul>
            </td>
        </tr>
    </table>
    <p></p>

    <table width="600">
        <tr>
            <td colspan="2">These relationships are representations of formal types described by the OBO-REL ontology:
                <a href="http://www.bioontology.org/wiki/index.php/RO:Main_Page">http://www.bioontology.org/wiki/index.php/RO:Main_Page</a>
            </td>
        </tr>
    </table>
</div>
