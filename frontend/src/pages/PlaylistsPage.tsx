import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchMyPlaylists, type Playlist } from '../api/catalogue';
import { FilmGrid } from '../components/FilmGrid/FilmGrid';
import { MovieCard } from '../components/MovieCard/MovieCard';
import { useAuth } from '../contexts/AuthContext';
import { useStarred } from '../contexts/StarredContext';
import { movieCardProps } from '../utils/movieCard';
import page from './Page.module.css';
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

  useEffect(() => {
    if (authState.status !== 'signed-in') {
      setState({ status: 'idle' });
      return;
    }

    let active = true;
    setState({ status: 'loading' });

    fetchMyPlaylists()
      .then((playlists) => {
        if (active) setState({ status: 'loaded', playlists });
      })
      .catch((error: unknown) => {
        if (active) {
          setState({
            status: 'failed',
            message: error instanceof Error ? error.message : 'Unknown error',
          });
        }
      });

    return () => {
      active = false;
    };
  }, [authState]);

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
          Sign in to see playlists owned by your account (seed users, mock users, or newly registered).
        </p>
        <Link className={page.textLink} to="/account">
          Sign in
        </Link>
      </>
    );
  }

  return (
    <>
      <h1 className={page.heading}>Your playlists</h1>
      <p className={page.lede}>Signed in as {authState.username}.</p>

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
        <p className={page.lede}>You do not have any playlists yet.</p>
      )}

      {state.status === 'loaded' && state.playlists.length > 0 && (
        <ul className={styles.list}>
          {state.playlists.map((playlist) => (
            <li key={playlist.id} className={styles.playlistCard}>
              <div className={styles.playlistHeader}>
                <h2 className={styles.playlistName}>{playlist.name}</h2>
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
