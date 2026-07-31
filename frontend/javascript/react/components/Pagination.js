import React from 'react';
import PropTypes from 'prop-types';

const PAGE_LIST_SIZE = 2;

const PageItem = ({className, ...rest}) => <li {...rest} className={`page-item ${className}`} />;
const PageLink = ({className, ...rest}) => <a {...rest} className={`page-link ${className}`} />;

const Pagination = ({ onChange, page, perPageSize, total }) => {

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
        pageLinks.push(<PageItem key='page-1'><PageLink href='#' onClick={() => onChange(1)}>1</PageLink></PageItem>);
    }
    if (start > 2) {
        pageLinks.push(<PageItem className='disabled' key='start-spacer'><PageLink>&hellip;</PageLink></PageItem>);
    }
    for (let i = start; i <= end; i++) {
        pageLinks.push(
            <PageItem className={i === page ? 'active' : ''} key={`page-${i}`}>
                <PageLink href='#' onClick={() => onChange(i)}>{i}</PageLink>
            </PageItem>
        );
    }
    if (end < totalPages - 1) {
        pageLinks.push(<PageItem className='disabled' key='end-spacer'><PageLink>&hellip;</PageLink></PageItem>);
    }
    if (end < totalPages) {
        pageLinks.push(<PageItem key='page-end'><PageLink href='#' onClick={() => onChange(totalPages)}>{totalPages}</PageLink></PageItem>);
    }
    return (
        <ul className='pagination'>
            <PageItem className={isFirstPage ? 'disabled' : ''}>
                <PageLink href='#' onClick={() => !isFirstPage && onChange(page - 1)}>&laquo; Prev</PageLink>
            </PageItem>
            {pageLinks}
            <PageItem className={isLastPage ? 'disabled' : ''}>
                <PageLink href='#' onClick={() => !isLastPage && onChange(page + 1)}>&raquo; Next</PageLink>
            </PageItem>
        </ul>
    );
};

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