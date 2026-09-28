import { useEffect, useId, useState } from 'react';
import { sortPlaylistsForPicker, useMyPlaylists } from '../../contexts/MyPlaylistsContext';
import styles from './PlaylistFilmDialog.module.css';

type Props = {
  tmdbId: number;
  filmLabel: string;
  onClose: () => void;
};

export function PlaylistFilmDialog({ tmdbId, filmLabel, onClose }: Props) {
  const titleId = useId();
  const {
    playlists,
    ready,
    isInPlaylist,
    addToPlaylist,
    removeFilmFromPlaylist,
    reload,
  } = useMyPlaylists();
  const [busyPlaylistId, setBusyPlaylistId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        onClose();
      }
    }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onClose]);

  async function toggleMembership(playlistId: number) {
    setError(null);
    setBusyPlaylistId(playlistId);
    try {
      if (isInPlaylist(tmdbId, playlistId)) {
        await removeFilmFromPlaylist(playlistId, tmdbId);
      } else {
        await addToPlaylist(playlistId, tmdbId);
      }
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not update playlist');
    } finally {
      setBusyPlaylistId(null);
    }
  }

  const sorted = sortPlaylistsForPicker(playlists);

  return (
    <div className={styles.backdrop} role="presentation" onClick={onClose}>
      <div
        className={styles.dialog}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={(event) => event.stopPropagation()}
      >
        <div className={styles.header}>
          <h2 className={styles.title} id={titleId}>
            Add to playlist
          </h2>
          <p>{filmLabel}</p>
        </div>

        {!ready && (
          <p className={styles.busy} role="status">
            Loading your playlists&hellip;
          </p>
        )}

        {ready && sorted.length === 0 && (
          <p className={styles.busy}>Create a playlist first on the Playlists page.</p>
        )}

        {error && (
          <p className={styles.error} role="alert">
            {error}
          </p>
        )}

        {ready && sorted.length > 0 && (
          <ul className={styles.list}>
            {sorted.map((playlist) => {
              const member = isInPlaylist(tmdbId, playlist.id);
              const busy = busyPlaylistId === playlist.id;
              return (
                <li key={playlist.id} className={styles.row}>
                  <span className={styles.rowLabel}>{playlist.name}</span>
                  <button
                    type="button"
                    className={`${styles.toggle} ${member ? styles.toggleActive : ''}`}
                    aria-pressed={member}
                    aria-label={
                      member
                        ? `Remove ${filmLabel} from ${playlist.name}`
                        : `Add ${filmLabel} to ${playlist.name}`
                    }
                    disabled={busy}
                    onClick={() => void toggleMembership(playlist.id)}
                  >
                    {member ? '\u2605' : '\u2606'}
                  </button>
                </li>
              );
            })}
          </ul>
        )}

        <div className={styles.footer}>
          <button type="button" className={styles.closeButton} onClick={onClose}>
            Done
          </button>
        </div>
      </div>
    </div>
  );
}
