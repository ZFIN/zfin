import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';
import TermLink from '../components/entity/TermLink';
import {EntityLink} from '../components/entity';
import Fish from '../components/Fish';

const PublicationDiseaseTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Human Disease',
            content: row => <TermLink key={row.diseaseAnnotation.disease} entity={row.diseaseAnnotation.disease}/>,
            width: '200px',
        },
        {
            label: 'Fish',
            content: row => row.fishExperiment && (<Fish entity={row.fishExperiment.fish}/>),
            width: '300px',
        },
        {
            label: 'Environment',
            content: row => row.fishExperiment && (<EntityLink entity={row.fishExperiment.experiment}/>),
            width: '350px',
        },
        {
            label: 'Evidence',
            content: row => row.diseaseAnnotation.evidenceCodeString,
            width: '80px',
        },
    ];

    const handleDataLoadedCount = (data) => {
        if (navigationCounter && navigationCounter.setCounts && data.total) {
            navigationCounter.setCounts(title, data.total);
        }
    };

    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={row => row.zdbID}
            pagination={true}
            onDataLoaded={handleDataLoadedCount}
        />
    );
};

PublicationDiseaseTable.propTypes = {
    url: PropTypes.string,
    title: PropTypes.string,
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default PublicationDiseaseTable;
