import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';

type StarredContextValue = {
  isStarred: (tmdbId: number) => boolean;
  toggleStarred: (tmdbId: number) => void;
};

const StarredContext = createContext<StarredContextValue | null>(null);

export function StarredProvider({ children }: { children: ReactNode }) {
  const [starred, setStarred] = useState<Set<number>>(() => new Set());

  const isStarred = useCallback((tmdbId: number) => starred.has(tmdbId), [starred]);

  const toggleStarred = useCallback((tmdbId: number) => {
    setStarred((previous) => {
      const next = new Set(previous);
      if (next.has(tmdbId)) {
        next.delete(tmdbId);
      } else {
        next.add(tmdbId);
      }
      return next;
    });
  }, []);

  const value = useMemo(() => ({ isStarred, toggleStarred }), [isStarred, toggleStarred]);

  return <StarredContext.Provider value={value}>{children}</StarredContext.Provider>;
}

export function useStarred(): StarredContextValue {
  const ctx = useContext(StarredContext);
  if (!ctx) {
    throw new Error('useStarred must be used within StarredProvider');
  }
  return ctx;
}
