import type { ReactNode } from 'react';
import styles from './FilmGrid.module.css';

type FilmGridProps = {
  children: ReactNode;
  compact?: boolean;
};

export function FilmGrid({ children, compact }: FilmGridProps) {
  return <div className={`${styles.grid} ${compact ? styles.gridCompact : ''}`}>{children}</div>;
}
