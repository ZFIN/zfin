import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';

const PublicationFishTable = ({url, title, navigationCounter}) => {
    const columns = [
        {
            label: 'Fish',
            content: (row) =>
                <span className='text-break'>
                    <a
                        className='text-break'
                        href={`/${row.zdbID}`}
                        dangerouslySetInnerHTML={{__html: row.displayName}}
                    />
                    <a
                        className='popup-link data-popup-link'
                        href={`/action/fish/fish-detail-popup/${row.zdbID}`}
                    />
                </span>
            ,
            width: '200px',
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

PublicationFishTable.propTypes = {
    url: PropTypes.string,
    title: PropTypes.string,
    navigationCounter: PropTypes.object,
};

export default PublicationFishTable;
