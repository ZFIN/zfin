import React from 'react';
import { string, node } from 'prop-types';

const ExternalLinkMaybe = ({href, className, children}) => {
    const isExternal = href && href.indexOf('http') === 0 && href.includes('zfin.org') === false;
    if (isExternal) {
        return <a href={href} className={'external' + (className ? ' ' + className : '') } target='_blank' rel='noopener noreferrer'>{children}</a>;
    } else {
        return <a href={href} className={className ? ' ' + className : ''}>{children}</a>;
    }
};

ExternalLinkMaybe.propTypes = {
    href: string,
    className: string,
    children: node,
}

export default ExternalLinkMaybe;
