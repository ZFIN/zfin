import React from 'react';
import PropTypes from 'prop-types';
import DataTable from '../components/data-table';

const PublicationFishTable = ({url}) => {
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
    return (
        <DataTable
            columns={columns}
            dataUrl={url}
            rowKey={row => row.zdbID}
            pagination={true}
        />
    );
};

PublicationFishTable.propTypes = {
    url: PropTypes.string,
};

export default PublicationFishTable;
