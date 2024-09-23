import React, {useEffect} from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import useTableState from "../hooks/useTableState";
import produce from "immer";

const EntityUpdatesTable = ({entityId, fieldNameFilter}) => {
    const [tableState, setTableState] = useTableState();

    useEffect(() => {
        if (fieldNameFilter) {
            setTableState(produce(state => {
                state.filter = {
                    fieldName: fieldNameFilter
                }
            }));
        }
    }, []);

    const columns = [
        {
            label: 'Submitter',
            content: ({submitterName, submitterZdbID}) => {
                return submitterZdbID ? <a href={'/' + submitterZdbID} target='_blank' rel='noreferrer'>{submitterName}</a> : <span>{submitterName}</span>;
            },
        },
        {
            label: 'Field',
            filterName: 'fieldName',
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
            tableState={tableState}
            setTableState={setTableState}
        />
    );
};

EntityUpdatesTable.propTypes = {
    entityId: PropTypes.string,
    fieldNameFilter: PropTypes.string,
};

export default EntityUpdatesTable;
