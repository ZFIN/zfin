/* eslint-disable react/no-render-return-value */
/* eslint-disable no-console */
import 'regenerator-runtime/runtime';
import React from 'react';
import ReactDOM from 'react-dom';
import {subscribe, setCounts} from './state/NavigationCounter';

const navigationCounter = {subscribe, setCounts};

document
    .querySelectorAll('.__react-root')
    .forEach(element => {
        // this split allows the same component to be used multiple times on the same page without
        // ID duplication. for example:
        //     <div class="__react-root" id="MyContainer__one"></div>
        //     <div class="__react-root" id="MyContainer__two"></div>
        const container = element.id.split('__', 1)[0];

        // this flag indicates that the component needs access to the navigation count state
        const useNavigationCount = element.classList.contains('__use-navigation-counter');
        const dataset = useNavigationCount ? {...element.dataset, navigationCounter} : {...element.dataset};

        import(`./containers/${container}`)
            .then(Module => ReactDOM.render(<Module.default {...dataset} />, element))
            .catch((error) => console.error('Unable to load container named: ' + container, error));
    });