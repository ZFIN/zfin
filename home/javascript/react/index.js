import React from 'react';
import ReactDOM from 'react-dom';

document
    .querySelectorAll('.__react-root')
    .forEach(element => {
        import(`./containers/${element.id}`)
            .then(Module => ReactDOM.render(<Module.default {...element.dataset} />, element))
            .catch((error) => console.error('Unable to load container for id: ' + element.id, error));
    });