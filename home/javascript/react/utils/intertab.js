const PREFIX = '__intertab_event_';

export default {
    addListener: (name, callback) => {
        window.addEventListener('storage', event => {
            if (event.key !== (PREFIX + name) || !event.newValue) {
                return;
            }
            callback(event);
        });
    },

    fireEvent: (name) => {
        window.localStorage.setItem(PREFIX + name, Math.random().toString(10));
        window.localStorage.removeItem(PREFIX + name);
    },

    EVENTS: {
        PUB_STATUS: 'pub_status'
    }
}
