import React from 'react';
import qs from 'qs';
import DataTable from '../data-table';
import {TermRelationshipTypeStat} from '../../containers/TermRelationshipListView';
import {TermLink} from './TermRelationshipInlineView';
import {RelatedTerm} from "./TermRelationshipView";

interface TermRelationshipTableProps {
    termId: string;
    relationshipType: TermRelationshipTypeStat;
}

const TermRelationshipTable = ({termId, relationshipType}: TermRelationshipTableProps) => {

    const columns = [
        {
            label: 'Term',
            content: (row: RelatedTerm) => <TermLink term={row}/>,
            width: '100px',
        }
    ];

    const params = {
        relationshipType: relationshipType.relationshipType,
        isForward: relationshipType.isForward,
    };

    return (
        <>
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/relationships?${qs.stringify(params)}`}
                rowKey={(row: RelatedTerm) => row.zdbID}
            />
        </>
    );
};

export default TermRelationshipTable;
