import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchMovies, type Movie } from '../api/catalogue';
import { FilmGrid } from '../components/FilmGrid/FilmGrid';
import { MovieCard } from '../components/MovieCard/MovieCard';
import { useStarred } from '../contexts/StarredContext';
import { movieCardProps } from '../utils/movieCard';
import page from './Page.module.css';

type LoadState =
  | { status: 'loading' }
  | { status: 'loaded'; movies: Movie[] }
  | { status: 'failed'; message: string };

export function CataloguePage() {
  const navigate = useNavigate();
  const { isStarred, toggleStarred } = useStarred();
  const [state, setState] = useState<LoadState>({ status: 'loading' });

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
    return () => {
      active = false;
    };
  }, []);

  return (
    <>
      <h1 className={page.heading}>Catalogue</h1>

      {state.status === 'loading' && (
        <p className={page.lede} role="status">
          Loading films&hellip;
        </p>
      )}

      {state.status === 'failed' && (
        <p className={page.error} role="alert">
          Could not load films: {state.message}. Is the backend running on port 8080?
        </p>
      )}

      {state.status === 'loaded' && (
        <>
          <p className={page.lede}>
            {state.movies.length} films from the seed playlists, ordered by combined rating.
          </p>

          <FilmGrid>
            {state.movies.map((movie) => (
              <MovieCard
                key={movie.tmdbId}
                {...movieCardProps(
                  movie,
                  isStarred(movie.tmdbId),
                  () => toggleStarred(movie.tmdbId),
                  () => navigate(`/movies/${movie.tmdbId}`),
                )}
              />
            ))}
          </FilmGrid>
        </>
      )}
    </>
  );
}
