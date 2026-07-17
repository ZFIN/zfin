import React from 'react';
import DataTable from '../components/data-table';
import qs from 'qs';

const RunIndexerTable = () => {

    const postRunIndexer = async (e) => {
        e.preventDefault();
        await fetch(e.target.href, {method: 'POST'});
        window.location.reload();
    }

    const columns = [
        {
            label: 'Name',
            content: row => row,
        },
        {
            label: 'Run',
            content: row => <a href={`/action/indexer/runIndexer/${row}`} onClick={postRunIndexer}>Run</a>,
        },
    ];


    const params = {};

    return (
        <>
            <h4>Runs:</h4>
            <DataTable
                columns={columns}
                dataUrl={`/action/api/indexer/config?${qs.stringify(params)}`}
                rowKey={row => row.id}
                pagination={false}
            />
        </>
    );
};

export default RunIndexerTable;
