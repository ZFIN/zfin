import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';

const EntityUpdatesTable = ({entityId}) => {

    const columns = [
        {
            label: 'Submitter',
            content: ({submitterName, submitterZdbID}) => {
                return submitterZdbID ? <a href={'/' + submitterZdbID} target='_blank' rel='noreferrer'>{submitterName}</a> : <span>{submitterName}</span>;
            },
        },
        {
            label: 'Field',
            content: ({fieldName}) => {
                return <span>{fieldName}</span>;
            },
        },
        {
            label: 'Old Value',
            content: ({oldValue}) => {
                return <div dangerouslySetInnerHTML={{__html: oldValue}} />;
            },
        },
        {
            label: 'New Value',
            content: ({newValue}) => {
                return <div dangerouslySetInnerHTML={{__html: newValue}} />;
            },
        },
        {
            label: 'Comments',
            content: ({comments}) => {
                return <div dangerouslySetInnerHTML={{__html: comments}} />;
            },
        },
        {
            label: 'When Updated',
            content: ({whenUpdated}) => {
                return <span>{whenUpdated}</span>;
            },
        }
    ];
    return (
        <DataTable
            columns={columns}
            dataUrl={`/action/api/updates/${entityId}`}
            rowKey={row => row.uniqueKey}
        />
    );
};

EntityUpdatesTable.propTypes = {
    entityId: PropTypes.string,
};

export default EntityUpdatesTable;
