import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { fetchStarredIds, starFilm, unstarFilm } from '../api/catalogue';
import { useAuth } from './AuthContext';
import { useMyPlaylists } from './MyPlaylistsContext';

type StarredContextValue = {
  isStarred: (tmdbId: number) => boolean;
  toggleStarred: (tmdbId: number) => Promise<void>;
  ready: boolean;
};

const StarredContext = createContext<StarredContextValue | null>(null);

export function StarredProvider({ children }: { children: ReactNode }) {
  const { state: authState } = useAuth();
  const { reload: reloadPlaylists } = useMyPlaylists();
  const [starred, setStarred] = useState<Set<number>>(() => new Set());
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (authState.status !== 'signed-in') {
      setStarred(new Set());
      setReady(true);
      return;
    }

    let active = true;
    setReady(false);
    fetchStarredIds()
      .then((payload) => {
        if (active) {
          setStarred(new Set(payload.tmdbIds));
          setReady(true);
        }
      })
      .catch(() => {
        if (active) {
          setStarred(new Set());
          setReady(true);
        }
      });

    return () => {
      active = false;
    };
  }, [authState.status, authState.status === 'signed-in' ? authState.username : '']);

  const isStarred = useCallback((tmdbId: number) => starred.has(tmdbId), [starred]);

  const toggleStarred = useCallback(
    async (tmdbId: number) => {
      if (authState.status !== 'signed-in') {
        return;
      }
      const wasStarred = starred.has(tmdbId);
      setStarred((previous) => {
        const next = new Set(previous);
        if (wasStarred) {
          next.delete(tmdbId);
        } else {
          next.add(tmdbId);
        }
        return next;
      });
      try {
        if (wasStarred) {
          await unstarFilm(tmdbId);
        } else {
          await starFilm(tmdbId);
        }
        await reloadPlaylists();
      } catch {
        setStarred((previous) => {
          const next = new Set(previous);
          if (wasStarred) {
            next.add(tmdbId);
          } else {
            next.delete(tmdbId);
          }
          return next;
        });
      }
    },
    [authState.status, starred, reloadPlaylists],
  );

  const value = useMemo(
    () => ({ isStarred, toggleStarred, ready }),
    [isStarred, toggleStarred, ready],
  );

  return <StarredContext.Provider value={value}>{children}</StarredContext.Provider>;
}

export function useStarred(): StarredContextValue {
  const ctx = useContext(StarredContext);
  if (!ctx) {
    throw new Error('useStarred must be used within StarredProvider');
  }
  return ctx;
}
