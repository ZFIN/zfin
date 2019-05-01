import React from 'react';
import DashboardFilterBar from "../components/DashboardFilterBar";

import { searchPubStatus } from "../api/publication";
import DashboardPubList from "../components/DashboardPubList";
import Pagination from "../components/Pagination";

const PUBS_PER_PAGE = 50;

class PubDashboard extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            page: 1,
            results: {},
            curator: '',
            order: '',
            status: '',
        };
        this.handleFilterChange = this.handleFilterChange.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
    }

    fetchPubs() {
        const params = {
            owner: this.state.curator,
            status: this.state.status,
            count: PUBS_PER_PAGE,
            offset: (this.state.page - 1) * PUBS_PER_PAGE,
            sort: this.state.order
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

    handleFilterChange(curator, order, status) {
        this.setState({curator, order, status}, () => this.fetchPubs());
    }

    handlePageChange(page) {
        this.setState({page}, () => this.fetchPubs());
    }

    render() {
        const { loading, page, results } = this.state;
        return (
            <div className="pub-dashboard">
                <DashboardFilterBar currentUserId="ZDB-PERS-140612-1" loading={loading} onChange={this.handleFilterChange}/>

                {loading && <div className="text-center text-muted">
                    <i className="fas fa-spinner fa-spin" /> Loading...
                </div>}

                {!loading && results.totalCount === 0 && <div className="text-center text-muted">
                    No pubs match query
                </div>}

                <DashboardPubList pubs={results.publications} statusCounts={results.statusCounts} />
                
                <div className="center">
                    <Pagination
                        onChange={this.handlePageChange}
                        page={page}
                        perPageSize={PUBS_PER_PAGE}
                        total={results.totalCount}
                    />
                </div>
            </div>
        );
    }
}

export default PubDashboard;
