import React from 'react';

const Supplier = ({suppliers}) => {
    if (suppliers) {
        return (suppliers.map((supplier) => {
            return <span key={supplier.organization.zdbID}>
                <a href={supplier.organization.zdbID}>{supplier.organization.name}</a>
                (<a href={supplier.orderURL}>order this <i className='fas fa-bags-shopping'/></a>)
                <p/>
            </span>;
        }))
    } else {
        return <span/>;
    }
};

export default Supplier;
