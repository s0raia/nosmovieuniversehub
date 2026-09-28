import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchHome, type HomeSection, type Movie } from '../api/catalogue';
import { HomeSection as HomeSectionBlock } from '../components/HomeSection/HomeSection';
import { useStarred } from '../contexts/StarredContext';
import page from './Page.module.css';

type LoadState =
  | { status: 'loading' }
  | { status: 'loaded'; sections: HomeSection[] }
  | { status: 'failed'; message: string };

export function HomePage() {
  const navigate = useNavigate();
  const { isStarred, toggleStarred } = useStarred();
  const [state, setState] = useState<LoadState>({ status: 'loading' });

  // Home is public: load sections immediately, without waiting for sign-in.
  useEffect(() => {
    let active = true;
    fetchHome()
      .then((sections) => {
        if (active) setState({ status: 'loaded', sections });
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

  const openMovie = (movie: Movie) => navigate(`/movies/${movie.tmdbId}`);

  return (
    <>
      <h1 className={page.heading}>Home</h1>
      <p className={page.lede}>
        Trending picks from TMDB plus highlights from playlists and ratings in MovieUniverse.
      </p>

      {state.status === 'loading' && (
        <p className={page.lede} role="status">
          Loading sections&hellip;
        </p>
      )}

      {state.status === 'failed' && (
        <p className={page.error} role="alert">
          Could not load home: {state.message}. Is the backend running on port 8080?
        </p>
      )}

      {state.status === 'loaded' &&
        state.sections.map((section) => (
          <HomeSectionBlock
            key={section.id}
            section={section}
            isStarred={isStarred}
            onToggleStarred={toggleStarred}
            onOpenMovie={openMovie}
          />
        ))}
    </>
  );
}
