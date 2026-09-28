import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createPlaylist, renamePlaylist } from '../../api/catalogue';
import { FilmGrid } from '../../components/FilmGrid/FilmGrid';
import { MovieCard } from '../../components/MovieCard/MovieCard';
import { useAuth } from '../../contexts/AuthContext';
import { useMyPlaylists } from '../../contexts/MyPlaylistsContext';
import { movieCardProps } from '../../utils/movieCard';
import page from '../../layouts/Page.module.css';
import styles from './PlaylistsPage.module.css';

export function PlaylistsPage() {
  const { state: authState } = useAuth();
  const { playlists, ready, reload, removeItem, moveItem } = useMyPlaylists();
  const navigate = useNavigate();
  const [newName, setNewName] = useState('');
  const [createError, setCreateError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState('');
  const [renameError, setRenameError] = useState<string | null>(null);
  const [moveTargets, setMoveTargets] = useState<Record<number, string>>({});
  const [itemError, setItemError] = useState<string | null>(null);
  const [busyItemId, setBusyItemId] = useState<number | null>(null);

  async function onCreate(event: FormEvent) {
    event.preventDefault();
    setCreateError(null);
    const trimmed = newName.trim();
    if (!trimmed) {
      setCreateError('Enter a playlist name');
      return;
    }
    setCreating(true);
    try {
      await createPlaylist(trimmed);
      setNewName('');
      await reload();
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : 'Could not create playlist');
    } finally {
      setCreating(false);
    }
  }

  function startRename(playlist: { id: number; name: string }) {
    setEditingId(playlist.id);
    setEditName(playlist.name);
    setRenameError(null);
  }

  function cancelRename() {
    setEditingId(null);
    setEditName('');
    setRenameError(null);
  }

  async function saveRename(playlistId: number) {
    setRenameError(null);
    const trimmed = editName.trim();
    if (!trimmed) {
      setRenameError('Name cannot be empty');
      return;
    }
    try {
      await renamePlaylist(playlistId, trimmed);
      setEditingId(null);
      await reload();
    } catch (err) {
      setRenameError(err instanceof Error ? err.message : 'Could not rename playlist');
    }
  }

  async function onRemove(playlistId: number, itemId: number) {
    setItemError(null);
    setBusyItemId(itemId);
    try {
      await removeItem(playlistId, itemId);
    } catch (err) {
      setItemError(err instanceof Error ? err.message : 'Could not remove film');
    } finally {
      setBusyItemId(null);
    }
  }

  async function onMove(itemId: number) {
    setItemError(null);
    const raw = moveTargets[itemId];
    const targetId = raw ? Number(raw) : NaN;
    if (!Number.isFinite(targetId)) {
      setItemError('Choose a playlist to move into');
      return;
    }
    setBusyItemId(itemId);
    try {
      await moveItem(itemId, targetId);
      setMoveTargets((previous) => {
        const next = { ...previous };
        delete next[itemId];
        return next;
      });
    } catch (err) {
      setItemError(err instanceof Error ? err.message : 'Could not move film');
    } finally {
      setBusyItemId(null);
    }
  }

  if (authState.status === 'checking') {
    return (
      <p className={page.lede} role="status">
        Checking session&hellip;
      </p>
    );
  }

  if (authState.status === 'anonymous') {
    return (
      <>
        <h1 className={page.heading}>Your playlists</h1>
        <p className={page.lede}>
          Log in to see playlists owned by your account (demo users or ones you registered).
        </p>
        <Link className={page.textLink} to="/account">
          Log in
        </Link>
      </>
    );
  }

  return (
    <>
      <h1 className={page.heading}>Your playlists</h1>
      <p className={page.lede}>
        Logged in as {authState.displayName ?? authState.username}. Add films from the catalog or
        film detail page; remove or move them here.
      </p>

      <form className={styles.createForm} onSubmit={onCreate}>
        <h2 className={styles.createHeading}>New playlist</h2>
        <p className={styles.createHint}>Playlist names can be in any language.</p>
        <div className={styles.createRow}>
          <label className={styles.createLabel}>
            Name
            <input
              className={styles.createInput}
              value={newName}
              maxLength={120}
              onChange={(event) => setNewName(event.target.value)}
              placeholder="e.g. Sunday sci-fi"
            />
          </label>
          <button className={styles.createButton} type="submit" disabled={creating}>
            {creating ? 'Creating…' : 'Create'}
          </button>
        </div>
        {createError && (
          <p className={page.formError} role="alert">
            {createError}
          </p>
        )}
      </form>

      {!ready && (
        <p className={page.lede} role="status">
          Loading playlists&hellip;
        </p>
      )}

      {itemError && (
        <p className={page.formError} role="alert">
          {itemError}
        </p>
      )}

      {ready && playlists.length === 0 && (
        <p className={page.lede}>You do not have any playlists yet. Create one above.</p>
      )}

      {ready && playlists.length > 0 && (
        <ul className={styles.list}>
          {playlists.map((playlist) => (
            <li key={playlist.id} className={styles.playlistCard}>
              <div className={styles.playlistHeader}>
                {editingId === playlist.id ? (
                  <div className={styles.renameBlock}>
                    <label className={styles.createLabel}>
                      Playlist name
                      <input
                        className={styles.createInput}
                        value={editName}
                        maxLength={120}
                        onChange={(event) => setEditName(event.target.value)}
                      />
                    </label>
                    <div className={styles.renameActions}>
                      <button
                        type="button"
                        className={styles.saveButton}
                        onClick={() => void saveRename(playlist.id)}
                      >
                        Save
                      </button>
                      <button type="button" className={styles.cancelButton} onClick={cancelRename}>
                        Cancel
                      </button>
                    </div>
                    {renameError && (
                      <p className={page.formError} role="alert">
                        {renameError}
                      </p>
                    )}
                  </div>
                ) : (
                  <>
                    <h2 className={styles.playlistName}>{playlist.name}</h2>
                    <button
                      type="button"
                      className={styles.editNameButton}
                      onClick={() => startRename(playlist)}
                    >
                      Edit name
                    </button>
                  </>
                )}
                <span className={styles.playlistMeta}>
                  {playlist.filmCount} films · owner {playlist.owner}
                </span>
              </div>

              {playlist.entries.length > 0 && (
                <FilmGrid compact className={styles.filmGrid}>
                  {playlist.entries.map((entry) => {
                    const otherPlaylists = playlists.filter(
                      (candidate) => candidate.id !== playlist.id,
                    );
                    return (
                      <div key={entry.itemId} className={styles.entryCell}>
                        <MovieCard
                          {...movieCardProps(
                            entry.film,
                            false,
                            () => {},
                            () => navigate(`/movies/${entry.film.tmdbId}`),
                            false,
                          )}
                        />
                        <div className={styles.filmActions}>
                          <button
                            type="button"
                            className={styles.removeButton}
                            disabled={busyItemId === entry.itemId}
                            onClick={() => void onRemove(playlist.id, entry.itemId)}
                          >
                            Remove
                          </button>
                          {otherPlaylists.length > 0 && (
                            <div className={styles.moveRow}>
                              <label className={styles.moveLabel}>
                                Move to
                                <select
                                  className={styles.moveSelect}
                                  value={moveTargets[entry.itemId] ?? ''}
                                  onChange={(event) =>
                                    setMoveTargets((previous) => ({
                                      ...previous,
                                      [entry.itemId]: event.target.value,
                                    }))
                                  }
                                >
                                  <option value="">Choose playlist</option>
                                  {otherPlaylists.map((target) => (
                                    <option key={target.id} value={target.id}>
                                      {target.name}
                                    </option>
                                  ))}
                                </select>
                              </label>
                              <button
                                type="button"
                                className={styles.moveButton}
                                disabled={busyItemId === entry.itemId}
                                onClick={() => void onMove(entry.itemId)}
                              >
                                Move
                              </button>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </FilmGrid>
              )}
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
