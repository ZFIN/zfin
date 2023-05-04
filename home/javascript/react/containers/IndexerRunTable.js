import React, {useState} from 'react';
import DataTable from '../components/data-table';
import qs from 'qs';
import Checkbox from '../components/Checkbox';

const IndexerRunTable = () => {

    const [showIndexerInfo, setShowIndexerInfo] = useState(false);
    const [showIndexerTask, setShowIndexerTask] = useState(false);
    const [runId, setRunId] = useState(null);
    const [runInfoId, setRunInfoId] = useState(null);

    const handleRunSelection = (runID) => {
        if (runID === runId) {
            setShowIndexerInfo(false)
            setRunId(null)
        } else {
            setShowIndexerInfo(true)
            setRunId(runID)
        }
    };

    const handleRunInfoSelection = (runInfoID) => {
        if (runInfoID === runInfoId) {
            setShowIndexerTask(false)
            setRunInfoId(null)
        } else {
            setShowIndexerTask(true)
            setRunInfoId(runInfoID)
        }
    };

    const columns = [
        {
            label: '',
            content: row => <div className='mb-2'>
                <Checkbox
                    checked={showIndexerInfo && row.id === runId}
                    id={row.id}
                    onClick={() => handleRunSelection(row.id)}
                />
            </div>
            ,
            width: '80px',
        },
        {
            label: 'ID',
            content: row => row.id,
            width: '80px',
        },
        {
            label: 'Start Day',
            content: row => row.startDay,
            width: '150px',
        },
        {
            label: 'Start Time',
            content: row => row.startTime,
            width: '150px',
        },
        {
            label: 'Duration',
            content: row => (<>{row.durationString && (<>{row.durationString.toLocaleString()}</>)}</>),
            align: 'right',
            width: '150px',
        },
        {
            label: 'End',
            content: row => row.endDate,
        },
    ];

    const columnIndexerInfo = [
        {
            label: '',
            content: row => <div className='mb-2'>
                <Checkbox
                    checked={showIndexerTask && row.id === runInfoId}
                    id={row.id}
                    onChange={() => handleRunInfoSelection(row.id)}
                />
            </div>
            ,
            width: '80px',
        },
        {
            label: 'ID',
            content: row => row.id,
            width: '80px',
        },
        {
            label: 'Name',
            content: row => row.name,
            width: '300px',
        },
        {
            label: 'Duration',
            content: row => (<>{row.duration && (<>{row.duration.toLocaleString()}</>)}</>),
            align: 'right',
            width: '80px',
        },
        {
            label: 'Count',
            content: row => (<>{row.duration && (<>{row.count.toLocaleString()}</>)}</>),
            align: 'right',
            width: '80px',
        },
        {
            label: 'Start',
            content: row => row.startDate,
            width: '400px',
        },
    ];

    const columnIndexerTask = [
        {
            label: 'ID',
            content: row => row.id,
            width: '80px',
        },
        {
            label: 'Name',
            content: row => row.name,
            width: '200px',
        },
        {
            label: 'Duration',
            content: row => (<>{row.duration && (<>{row.duration.toLocaleString()}</>)}</>),
            align: 'right',
            width: '80px',
        },
        {
            label: 'Start',
            content: row => row.startDate,
            width: '500px',
        },
    ];

    const params = {};
    const paramsInfo = {};
    const paramsTask = {};


    return (
        <>
            <h4>Runs:</h4>
            <DataTable
                columns={columns}
                dataUrl={`/action/api/indexer/runs?${qs.stringify(params)}`}
                rowKey={row => row.id}
                pagination={true}
            />
            <p/>
            {showIndexerInfo && (
                <>
                    <h4>Run: {runId}</h4>
                    <DataTable
                        columns={columnIndexerInfo}
                        dataUrl={`/action/api/indexer/run/${runId}?${qs.stringify(paramsInfo)}`}
                        rowKey={row => row.id}
                        pagination={false}
                    />
                    {showIndexerTask && (
                        <>
                            <p/>
                            <h4>Run Info: {runInfoId}</h4>
                            <DataTable
                                columns={columnIndexerTask}
                                dataUrl={`/action/api/indexer/run/${runId}/info/${runInfoId}?${qs.stringify(paramsTask)}`}
                                rowKey={row => row.id}
                                pagination={false}
                            />
                        </>
                    )}

                </>
            )}
        </>
    );
};

export default IndexerRunTable;
