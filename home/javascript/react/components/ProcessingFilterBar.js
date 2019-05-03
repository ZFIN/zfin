import React from 'react';
import PropTypes from 'prop-types';
import RefreshButton from "./RefreshButton";

const SORT_OPTIONS = [
    {
        value: 'date',
        display: 'Time in bin (oldest)'
    },
    {
        value: '-date',
        display: 'Time in bin (newest)'
    },
];

class ProcessingFilterBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            sort: SORT_OPTIONS[0].value
        };
        this.handleSortChange = this.handleSortChange.bind(this);
        this.fireCallback = this.fireCallback.bind(this);
    }

    componentDidMount() {
        this.fireCallback();
    }

    handleSortChange(event) {
        this.setState({
            sort: event.target.value
        }, () => this.fireCallback());
    }

    fireCallback() {
        this.props.onChange(this.state.sort);
    }

    render() {
        const { loading, pubCount } = this.props;
        const { sort } = this.state;
        return (
            <div className="row filter-bar">
                <div className="col-sm-12">
                    <form className="form-inline">
                        <b>{loading ? '▒' : pubCount}</b> Pubs ready for processing by
                        <select className="form-control" value={sort} onChange={this.handleSortChange}>
                            {SORT_OPTIONS.map(option => (
                                <option key={option.value} value={option.value}>{option.display}</option>
                            ))}
                        </select>
                        <RefreshButton loading={loading} onClick={this.fireCallback} />
                    </form>
                </div>
            </div>
        );
    }
}

ProcessingFilterBar.propTypes = {
    loading: PropTypes.bool,
    onChange: PropTypes.func,
    pubCount: PropTypes.number,
};

export default ProcessingFilterBar;
