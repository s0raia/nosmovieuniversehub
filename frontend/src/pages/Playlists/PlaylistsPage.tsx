import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  createPlaylist,
  fetchMyPlaylists,
  renamePlaylist,
  type Playlist,
} from '../../api/catalogue';
import { FilmGrid } from '../../components/FilmGrid/FilmGrid';
import { MovieCard } from '../../components/MovieCard/MovieCard';
import { useAuth } from '../../contexts/AuthContext';
import { useStarred } from '../../contexts/StarredContext';
import { movieCardProps } from '../../utils/movieCard';
import page from '../../layouts/Page.module.css';
import styles from './PlaylistsPage.module.css';

type LoadState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'loaded'; playlists: Playlist[] }
  | { status: 'failed'; message: string };

export function PlaylistsPage() {
  const { state: authState } = useAuth();
  const { isStarred, toggleStarred } = useStarred();
  const navigate = useNavigate();
  const [state, setState] = useState<LoadState>({ status: 'idle' });
  const [newName, setNewName] = useState('');
  const [createError, setCreateError] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState('');
  const [renameError, setRenameError] = useState<string | null>(null);

  const loadPlaylists = useCallback(async () => {
    setState({ status: 'loading' });
    try {
      const playlists = await fetchMyPlaylists();
      setState({ status: 'loaded', playlists });
    } catch (error: unknown) {
      setState({
        status: 'failed',
        message: error instanceof Error ? error.message : 'Unknown error',
      });
    }
  }, []);

  useEffect(() => {
    if (authState.status !== 'signed-in') {
      setState({ status: 'idle' });
      return;
    }
    void loadPlaylists();
  }, [authState, loadPlaylists]);

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
      await loadPlaylists();
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : 'Could not create playlist');
    } finally {
      setCreating(false);
    }
  }

  function startRename(playlist: Playlist) {
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
      await loadPlaylists();
    } catch (err) {
      setRenameError(err instanceof Error ? err.message : 'Could not rename playlist');
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
        Logged in as {authState.displayName ?? authState.username}.
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

      {state.status === 'loading' && (
        <p className={page.lede} role="status">
          Loading playlists&hellip;
        </p>
      )}

      {state.status === 'failed' && (
        <p className={page.error} role="alert">
          Could not load playlists: {state.message}
        </p>
      )}

      {state.status === 'loaded' && state.playlists.length === 0 && (
        <p className={page.lede}>You do not have any playlists yet. Create one above.</p>
      )}

      {state.status === 'loaded' && state.playlists.length > 0 && (
        <ul className={styles.list}>
          {state.playlists.map((playlist) => (
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

              {playlist.films.length > 0 && (
                <div className={styles.films}>
                  <FilmGrid compact>
                    {playlist.films.map((movie, index) => (
                      <MovieCard
                        key={`${playlist.id}-${movie.tmdbId}-${index}`}
                        {...movieCardProps(
                          movie,
                          isStarred(movie.tmdbId),
                          () => toggleStarred(movie.tmdbId),
                          () => navigate(`/movies/${movie.tmdbId}`),
                          true,
                        )}
                      />
                    ))}
                  </FilmGrid>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
