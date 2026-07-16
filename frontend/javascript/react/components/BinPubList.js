import React from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';

const BinPubList = ({ loading, pubs, columns }) => {
    return (
        <React.Fragment>
            <table className='table'>
                <thead>
                    <tr>
                        {columns.map(column => <th key={`label-${column.label}`} width={column.width}>{column.label}</th>)}
                    </tr>
                </thead>
                <tbody>
                    {!loading && pubs && pubs.map((pub, index) => (
                        <tr key={pub.zdbId}>
                            {columns.map(column => <td key={`${column.label}-${pub.zdbId}`}>{column.content(pub, index)}</td>)}
                        </tr>
                    ))}
                </tbody>
            </table>
            {loading &&
                <div className='text-muted text-center'>
                    <LoadingSpinner /> Loading...
                </div>
            }
            {!loading && pubs && pubs.length === 0 &&
                <div className='text-muted text-center'>
                    Woo hoo! Bin is empty!
                </div>
            }
        </React.Fragment>
    );
};

BinPubList.propTypes = {
    columns: PropTypes.array,
    loading: PropTypes.bool,
    pubs: PropTypes.array,
};

export default BinPubList;
