import { useEffect, useState } from 'react';
import { AppShell } from './components/AppShell/AppShell';
import { MovieCard } from './components/MovieCard/MovieCard';
import { fetchMovies, type Movie } from './api/catalogue';
import styles from './App.module.css';

type LoadState =
  | { status: 'loading' }
  | { status: 'loaded'; movies: Movie[] }
  | { status: 'failed'; message: string };

export default function App() {
  const [state, setState] = useState<LoadState>({ status: 'loading' });
  /*
    Keyed by TMDB id, not by title. The catalogue holds two films called Dune
    and two called The Lion King, so a title would conflate them and starring
    one would star the other.
  */
  const [starred, setStarred] = useState<Set<number>>(new Set());

  useEffect(() => {
    let active = true;

    fetchMovies()
      .then((movies) => {
        if (active) setState({ status: 'loaded', movies });
      })
      .catch((error: unknown) => {
        if (active) {
          setState({
            status: 'failed',
            message: error instanceof Error ? error.message : 'Unknown error',
          });
        }
      });

    // Guards against setting state after the component has gone away.
    return () => {
      active = false;
    };
  }, []);

  const toggle = (tmdbId: number) =>
    setStarred((previous) => {
      const next = new Set(previous);
      if (next.has(tmdbId)) {
        next.delete(tmdbId);
      } else {
        next.add(tmdbId);
      }
      return next;
    });

  return (
    <AppShell>
      <h1 className={styles.heading}>Catalogue</h1>

      {state.status === 'loading' && (
        <p className={styles.lede} role="status">
          Loading films&hellip;
        </p>
      )}

      {state.status === 'failed' && (
        <p className={styles.error} role="alert">
          Could not load films: {state.message}. Is the backend running on port 8080?
        </p>
      )}

      {state.status === 'loaded' && (
        <>
          <p className={styles.lede}>
            {state.movies.length} films from the seed playlists, ordered by combined rating.
          </p>

          <div className={styles.grid}>
            {state.movies.map((movie) => (
              <MovieCard
                key={movie.tmdbId}
                title={movie.title ?? 'Untitled'}
                year={movie.releaseYear}
                posterUrl={movie.posterUrl}
                tmdbAverage={movie.tmdbVoteAverage}
                tmdbVotes={movie.tmdbVoteCount}
                combinedScore={movie.combinedRating}
                combinedVotes={movie.tmdbVoteCount + movie.localVoteCount}
                inPlaylist={starred.has(movie.tmdbId)}
                onTogglePlaylist={() => toggle(movie.tmdbId)}
              />
            ))}
          </div>
        </>
      )}
    </AppShell>
  );
}
