import React from 'react';
import PropTypes from 'prop-types';

const PAGE_LIST_SIZE = 2;

class Pagination extends React.Component {
    render() {
        const { onChange, page, perPageSize, total } = this.props;
        const isFirstPage = page === 1;
        const totalPages = Math.ceil(total / perPageSize);
        if (!total || !totalPages || totalPages === 1) {
            return null;
        }
        const isLastPage = page === totalPages;
        const pageLinks = [];
        const start = Math.max(page - PAGE_LIST_SIZE, 1);
        const end = Math.min(page + PAGE_LIST_SIZE, totalPages);
        if (start > 1) {
            pageLinks.push(<li key='page-1'><a href='#' onClick={() => onChange(1)}>1</a></li>);
        }
        if (start > 2) {
            pageLinks.push(<li className='disabled' key='start-spacer'><a>&hellip;</a></li>);
        }
        for (let i = start; i <= end; i++) {
            pageLinks.push(
                <li className={i === page ? 'active' : ''} key={`page-${i}`}>
                    <a href="#" onClick={() => onChange(i)}>{i}</a>
                </li>
            );
        }
        if (end < totalPages - 1) {
            pageLinks.push(<li className='disabled' key='end-spacer'><a>&hellip;</a></li>);
        }
        if (end < totalPages) {
            pageLinks.push(<li key='page-end'><a href='#' onClick={() => onChange(totalPages)}>{totalPages}</a></li>);
        }
        return (
            <ul className="pagination">
                <li className={isFirstPage ? 'disabled' : ''}>
                    <a href="#" onClick={() => !isFirstPage && onChange(page - 1)}>&laquo; Prev</a>
                </li>
                {pageLinks}
                <li className={isLastPage ? 'disabled' : ''}>
                    <a href="#" onClick={() => !isLastPage && onChange(page + 1)}>&raquo; Next</a>
                </li>
            </ul>
        );
    }
}

Pagination.propTypes = {
    onChange: PropTypes.func,
    page: PropTypes.number.isRequired,
    total: PropTypes.number,
    perPageSize: PropTypes.number.isRequired,
};

Pagination.defaultProps = {
    initialPage: 1
};

export default Pagination;