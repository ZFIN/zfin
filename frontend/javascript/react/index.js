 
import 'regenerator-runtime/runtime';
import React from 'react';
import { createRoot } from 'react-dom/client';
import * as navigationCounter from './state/NavigationCounter';
import * as zfinEventBus from './state/ZfinEventBus';

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

        // this flag indicates that the component needs access to the event bus state
        const useEventBus = element.classList.contains('__use-event-bus');

        //build the dataset
        let dataset = useNavigationCount ? {...element.dataset, navigationCounter} : {...element.dataset};
        dataset = useEventBus ? {...dataset, eventBus: zfinEventBus} : {...dataset};

        //check for innerHTML
        if (element.innerHTML) {
            dataset.innerHTML = element.innerHTML;
        }

        const root = createRoot(element);
        import(`./containers/${container}`)
            .then(Module => root.render(<Module.default {...dataset} />))
            .catch((error) => console.error('Unable to load container named: ' + container, error));
    });
