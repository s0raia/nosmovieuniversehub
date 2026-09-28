import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import {
  addPlaylistItem,
  fetchMyPlaylists,
  movePlaylistItem,
  removePlaylistItem,
  type Playlist,
} from '../api/catalogue';
import { useAuth } from './AuthContext';

type MyPlaylistsContextValue = {
  playlists: Playlist[];
  ready: boolean;
  reload: () => Promise<void>;
  isInAnyPlaylist: (tmdbId: number) => boolean;
  isInPlaylist: (tmdbId: number, playlistId: number) => boolean;
  entriesForFilm: (tmdbId: number, playlistId: number) => { itemId: number }[];
  addToPlaylist: (playlistId: number, tmdbId: number) => Promise<void>;
  removeFilmFromPlaylist: (playlistId: number, tmdbId: number) => Promise<void>;
  removeItem: (playlistId: number, itemId: number) => Promise<void>;
  moveItem: (itemId: number, targetPlaylistId: number) => Promise<void>;
};

const MyPlaylistsContext = createContext<MyPlaylistsContextValue | null>(null);

export function MyPlaylistsProvider({ children }: { children: ReactNode }) {
  const { state: authState } = useAuth();
  const [playlists, setPlaylists] = useState<Playlist[]>([]);
  const [ready, setReady] = useState(false);

  const reload = useCallback(async () => {
    if (authState.status !== 'signed-in') {
      setPlaylists([]);
      setReady(true);
      return;
    }
    setReady(false);
    try {
      const next = await fetchMyPlaylists();
      setPlaylists(next);
    } catch {
      setPlaylists([]);
    } finally {
      setReady(true);
    }
  }, [authState.status, authState.status === 'signed-in' ? authState.username : '']);

  useEffect(() => {
    void reload();
  }, [reload]);

  const isInPlaylist = useCallback(
    (tmdbId: number, playlistId: number) =>
      playlists.some(
        (playlist) =>
          playlist.id === playlistId &&
          playlist.entries.some((entry) => entry.film.tmdbId === tmdbId),
      ),
    [playlists],
  );

  const isInAnyPlaylist = useCallback(
    (tmdbId: number) =>
      playlists.some((playlist) =>
        playlist.entries.some((entry) => entry.film.tmdbId === tmdbId),
      ),
    [playlists],
  );

  const entriesForFilm = useCallback(
    (tmdbId: number, playlistId: number) => {
      const playlist = playlists.find((p) => p.id === playlistId);
      if (!playlist) {
        return [];
      }
      return playlist.entries
        .filter((entry) => entry.film.tmdbId === tmdbId)
        .map((entry) => ({ itemId: entry.itemId }));
    },
    [playlists],
  );

  const addToPlaylist = useCallback(
    async (playlistId: number, tmdbId: number) => {
      await addPlaylistItem(playlistId, tmdbId);
      await reload();
    },
    [reload],
  );

  const removeItem = useCallback(
    async (playlistId: number, itemId: number) => {
      await removePlaylistItem(playlistId, itemId);
      await reload();
    },
    [reload],
  );

  const removeFilmFromPlaylist = useCallback(
    async (playlistId: number, tmdbId: number) => {
      const matches = entriesForFilm(tmdbId, playlistId);
      for (const match of matches) {
        await removePlaylistItem(playlistId, match.itemId);
      }
      await reload();
    },
    [entriesForFilm, reload],
  );

  const moveItem = useCallback(
    async (itemId: number, targetPlaylistId: number) => {
      await movePlaylistItem(itemId, targetPlaylistId);
      await reload();
    },
    [reload],
  );

  const value = useMemo(
    () => ({
      playlists,
      ready,
      reload,
      isInAnyPlaylist,
      isInPlaylist,
      entriesForFilm,
      addToPlaylist,
      removeFilmFromPlaylist,
      removeItem,
      moveItem,
    }),
    [
      playlists,
      ready,
      reload,
      isInAnyPlaylist,
      isInPlaylist,
      entriesForFilm,
      addToPlaylist,
      removeFilmFromPlaylist,
      removeItem,
      moveItem,
    ],
  );

  return <MyPlaylistsContext.Provider value={value}>{children}</MyPlaylistsContext.Provider>;
}

export function useMyPlaylists(): MyPlaylistsContextValue {
  const ctx = useContext(MyPlaylistsContext);
  if (!ctx) {
    throw new Error('useMyPlaylists must be used within MyPlaylistsProvider');
  }
  return ctx;
}

export function sortPlaylistsForPicker(playlists: Playlist[]): Playlist[] {
  return [...playlists].sort((left, right) => {
    const leftStarred = left.externalId?.startsWith('starred-') ? 0 : 1;
    const rightStarred = right.externalId?.startsWith('starred-') ? 0 : 1;
    if (leftStarred !== rightStarred) {
      return leftStarred - rightStarred;
    }
    return left.name.localeCompare(right.name, 'en');
  });
}
