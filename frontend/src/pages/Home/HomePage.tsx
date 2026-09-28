import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchHome, fetchMyPlaylists, type HomeSection, type Movie, type Playlist } from '../../api/catalogue';
import { HomeSection as HomeSectionBlock } from '../../components/HomeSection/HomeSection';
import { useAuth } from '../../contexts/AuthContext';
import { useStarred } from '../../contexts/StarredContext';
import page from '../../layouts/Page.module.css';
import homeStyles from './HomePage.module.css';

type LoadState =
  | { status: 'loading' }
  | { status: 'loaded'; sections: HomeSection[] }
  | { status: 'failed'; message: string };

export function HomePage() {
  const navigate = useNavigate();
  const { state: authState } = useAuth();
  const { isStarred, toggleStarred } = useStarred();
  const showPlaylistStar = authState.status === 'signed-in';
  const [state, setState] = useState<LoadState>({ status: 'loading' });
  const [myPlaylists, setMyPlaylists] = useState<Playlist[]>([]);

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

  useEffect(() => {
    if (authState.status !== 'signed-in') {
      setMyPlaylists([]);
      return;
    }
    let active = true;
    fetchMyPlaylists()
      .then((playlists) => {
        if (active) setMyPlaylists(playlists);
      })
      .catch(() => {
        if (active) setMyPlaylists([]);
      });
    return () => {
      active = false;
    };
  }, [authState.status]);

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

      {authState.status === 'signed-in' && myPlaylists.length > 0 && (
        <section className={homeStyles.myPlaylists} aria-labelledby="home-my-playlists">
          <h2 className={homeStyles.myPlaylistsHeading} id="home-my-playlists">
            Your playlists
          </h2>
          <ul className={homeStyles.myPlaylistsList}>
            {myPlaylists.slice(0, 4).map((playlist) => (
              <li key={playlist.id} className={homeStyles.myPlaylistItem}>
                <strong>{playlist.name}</strong> · {playlist.filmCount} films
              </li>
            ))}
          </ul>
          <Link className={homeStyles.myPlaylistsLink} to="/playlists">
            Manage playlists
          </Link>
        </section>
      )}

      {state.status === 'loaded' && (
        <div className={homeStyles.sections}>
          {state.sections.map((section) => (
            <HomeSectionBlock
              key={section.id}
              section={section}
              showPlaylistStar={showPlaylistStar}
              isStarred={isStarred}
              onToggleStarred={toggleStarred}
              onOpenMovie={openMovie}
            />
          ))}
        </div>
      )}
    </>
  );
}
