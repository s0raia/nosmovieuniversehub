import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { fetchHome, type HomeSection, type Movie } from '../../api/catalogue';
import { HomeSection as HomeSectionBlock } from '../../components/HomeSection/HomeSection';
import { PlaylistFilmDialog } from '../../components/PlaylistFilmDialog/PlaylistFilmDialog';
import { useAuth } from '../../contexts/AuthContext';
import { useMyPlaylists } from '../../contexts/MyPlaylistsContext';
import page from '../../layouts/Page.module.css';
import homeStyles from './HomePage.module.css';

type LoadState =
  | { status: 'loading' }
  | { status: 'loaded'; sections: HomeSection[] }
  | { status: 'failed'; message: string };

export function HomePage() {
  const navigate = useNavigate();
  const { state: authState } = useAuth();
  const { playlists: myPlaylists, isInAnyPlaylist } = useMyPlaylists();
  const showPlaylistStar = authState.status === 'signed-in';
  const [state, setState] = useState<LoadState>({ status: 'loading' });
  const [picker, setPicker] = useState<{ tmdbId: number; label: string } | null>(null);

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

  function openPlaylistPicker(movie: Movie) {
    const label = movie.releaseYear
      ? `${movie.title ?? 'Untitled'} (${movie.releaseYear})`
      : (movie.title ?? 'Untitled');
    setPicker({ tmdbId: movie.tmdbId, label });
  }

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
              isInAnyPlaylist={isInAnyPlaylist}
              onManagePlaylists={openPlaylistPicker}
              onOpenMovie={openMovie}
            />
          ))}
        </div>
      )}

      {picker && (
        <PlaylistFilmDialog
          tmdbId={picker.tmdbId}
          filmLabel={picker.label}
          onClose={() => setPicker(null)}
        />
      )}
    </>
  );
}
