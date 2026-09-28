import type { HomeSection as HomeSectionData, Movie } from '../../api/catalogue';
import { FilmGrid } from '../FilmGrid/FilmGrid';
import { MovieCard } from '../MovieCard/MovieCard';
import { movieCardProps } from '../../utils/movieCard';
import styles from './HomeSection.module.css';

type HomeSectionProps = {
  section: HomeSectionData;
  isStarred: (tmdbId: number) => boolean;
  onToggleStarred: (tmdbId: number) => void;
  onOpenMovie: (movie: Movie) => void;
};

export function HomeSection({ section, isStarred, onToggleStarred, onOpenMovie }: HomeSectionProps) {
  return (
    <section className={styles.section} aria-labelledby={`section-${section.id}`}>
      <h2 className={styles.heading} id={`section-${section.id}`}>
        {section.title}
      </h2>

      {section.films.length === 0 ? (
        <p className={styles.empty}>No films to show yet.</p>
      ) : (
        <FilmGrid compact>
          {section.films.map((movie) => (
            <MovieCard
              key={movie.tmdbId}
              {...movieCardProps(
                movie,
                isStarred(movie.tmdbId),
                () => onToggleStarred(movie.tmdbId),
                () => onOpenMovie(movie),
              )}
            />
          ))}
        </FilmGrid>
      )}
    </section>
  );
}
