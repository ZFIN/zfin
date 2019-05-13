import React from 'react';
import PropTypes from 'prop-types';
import RelativeDate from "./RelativeDate";

const ProcessingBinList = ({ loading, pubs, onClaimPub }) => {
    return (
        <table className='table'>
            <thead>
            <tr>
                <th width="115px"></th>
                <th width="150px">ZDB-ID</th>
                <th>Details</th>
                <th width="100px">Time in bin</th>
                <th width="50px">PDF</th>
            </tr>
            </thead>
            <tbody>
            {loading &&
                <tr>
                    <td className="text-muted text-center" colSpan="5">
                        <i className="fas fa-spinner fa-spin" /> Loading...
                    </td>
                </tr>
            }
            {!loading && pubs && pubs.length === 0 &&
                <tr>
                    <td className="text-muted text-center" colSpan="5">Woo hoo! Bin is empty!</td>
                </tr>
            }
            {!loading && pubs && pubs.map((pub, index) => (
                <tr key={pub.zdbId}>
                    <td>
                        <button className={`btn ${pub.claimed ? 'btn-success' : 'btn-default'}`}
                                disabled={pub.saving}
                                onClick={() => onClaimPub(pub, index)}
                        >
                            {pub.saving && <span><i className="fas fa-spinner fa-spin" /></span>}
                            {!pub.saving && pub.claimed && <span><i className="fas fa-check" /> Claimed</span>}
                            {!pub.saving && !pub.claimed && <span>Claim</span>}
                        </button>
                        {pub.claimError && <p className='text-danger'>{pub.claimError}</p>}
                    </td>
                    <td><a href={`/${pub.zdbId}`} target="_blank" rel="noopener noreferrer">{pub.zdbId}</a></td>
                    <td>
                        <p><b dangerouslySetInnerHTML={{__html: pub.title}} /></p>
                        <p>{pub.citation}</p>
                    </td>
                    <td><RelativeDate date={pub.status.updateDate} /></td>
                    <td>
                        {pub.pdfPath && <a href={`/PDFLoadUp/${pub.pdfPath}`} target='_blank' rel="noopener noreferrer"><i className="far fa-file-pdf"/></a>}
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    );
};

ProcessingBinList.propTypes = {
    loading: PropTypes.bool,
    pubs: PropTypes.array,
    onClaimPub: PropTypes.func,
};

export default ProcessingBinList;
