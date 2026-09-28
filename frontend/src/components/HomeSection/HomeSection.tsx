import type { HomeSection as HomeSectionData, Movie } from '../../api/catalogue';
import { FilmGrid } from '../FilmGrid/FilmGrid';
import { MovieCard } from '../MovieCard/MovieCard';
import { movieCardProps } from '../../utils/movieCard';
import styles from './HomeSection.module.css';

const SECTION_HINTS: Record<string, string> = {
  'latest-releases': 'Released in the last six months.',
  anticipated: 'Coming soon to theaters.',
};

type HomeSectionProps = {
  section: HomeSectionData;
  showPlaylistStar: boolean;
  isInAnyPlaylist: (tmdbId: number) => boolean;
  onManagePlaylists: (movie: Movie) => void;
  onOpenMovie: (movie: Movie) => void;
};

export function HomeSection({
  section,
  showPlaylistStar,
  isInAnyPlaylist,
  onManagePlaylists,
  onOpenMovie,
}: HomeSectionProps) {
  return (
    <section className={styles.section} aria-labelledby={`section-${section.id}`}>
      <h2 className={styles.heading} id={`section-${section.id}`}>
        {section.title}
      </h2>

      {SECTION_HINTS[section.id] && (
        <p className={styles.subheading}>{SECTION_HINTS[section.id]}</p>
      )}

      {section.films.length === 0 ? (
        <p className={styles.empty}>No films to show yet.</p>
      ) : (
        <FilmGrid compact>
          {section.films.map((movie) => (
            <MovieCard
              key={movie.tmdbId}
              {...movieCardProps(
                movie,
                isInAnyPlaylist(movie.tmdbId),
                () => onManagePlaylists(movie),
                () => onOpenMovie(movie),
                showPlaylistStar,
              )}
            />
          ))}
        </FilmGrid>
      )}
    </section>
  );
}
