import { QueryClient } from '@tanstack/react-query';

// Singleton shared by every ZIRC mount on the page. The mount system creates
// one root per element, but they all share this QueryClient so cache reads
// across mounts (e.g. dashboard + edit) stay consistent.
export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 30_000,
            refetchOnWindowFocus: false,
        },
    },
});
