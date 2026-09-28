import type { ReactNode } from 'react';
import styles from './FilmGrid.module.css';

type FilmGridProps = {
  children: ReactNode;
  compact?: boolean;
  className?: string;
};

export function FilmGrid({ children, compact, className }: FilmGridProps) {
  const gridClass = `${styles.grid} ${compact ? styles.gridCompact : ''}${className ? ` ${className}` : ''}`;
  return <div className={gridClass.trim()}>{children}</div>;
}
