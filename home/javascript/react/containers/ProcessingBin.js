import React from 'react';
import PropTypes from 'prop-types';
import update from 'immutability-helper';
import ProcessingFilterBar from "../components/ProcessingFilterBar";
import {searchPubStatus, updateStatus} from "../api/publication";
import ProcessingBinList from "../components/ProcessingBinList";
import Pagination from "../components/Pagination";

const PUBS_PER_PAGE = 50;

class ProcessingBin extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            page: 1,
            loading: false,
            results: {},
            sort: '',
        };
        this.handleFilterChange = this.handleFilterChange.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
        this.updatePubStatus = this.updatePubStatus.bind(this);
    }

    fetchPubs() {
        const { currentStatus } = this.props;
        const { sort, page } = this.state;
        const params = {
            status: currentStatus,
            sort: sort,
            count: PUBS_PER_PAGE,
            offset: (page - 1) * PUBS_PER_PAGE
        };
        this.setState({
            results: {},
            loading: true
        });
        searchPubStatus(params)
            .then(results => this.setState({
                results,
                loading: false
            }));
    }

    setPubState(index, field, value) {
        this.setState({
            results: update(this.state.results, {publications: {[index]: {[field]: {$set: value}}}})
        });
    }

    updatePubStatus(pub, index) {
        const { nextStatus, userId } = this.props;
        this.setPubState(index, 'saving', true);
        const status = {
            pubZdbID: pub.zdbId,
            status: { id: nextStatus },
            location: null,
            owner: { zdbID: userId }
        };
        updateStatus(status, true)
            .then(() => this.setPubState(index, 'claimed', true))
            .fail(xhr => xhr.responseJSON && this.setPubState(index, 'claimError', xhr.responseJSON.message))
            .always(() => this.setPubState(index, 'saving', false));
    }

    handleFilterChange(sort) {
        this.setState({sort}, () => this.fetchPubs());
    }

    handlePageChange(page) {
        this.setState({page}, () => this.fetchPubs());
    }

    render() {
        const { loading, page, results } = this.state;
        return (
            <div className="pub-dashboard">
                <ProcessingFilterBar loading={loading}
                                     onChange={this.handleFilterChange}
                                     pubCount={results.totalCount}
                />
                <ProcessingBinList loading={loading}
                                   onClaimPub={this.updatePubStatus}
                                   pubs={results.publications}
                />
                <div className='center'>
                    <Pagination onChange={this.handlePageChange}
                                page={page}
                                perPageSize={PUBS_PER_PAGE}
                                total={results.totalCount}
                    />
                </div>
            </div>
        );
    }
}

ProcessingBin.propTypes = {
    currentStatus: PropTypes.string,
    nextStatus: PropTypes.string,
    userId: PropTypes.string,
};

export default ProcessingBin;
