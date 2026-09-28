import type { ReactNode } from 'react';
import { AppShell } from '../components/AppShell/AppShell';

type MainLayoutProps = {
  children: ReactNode;
};

/** Site chrome shared by all public routes (nav, auth badge). */
export function MainLayout({ children }: MainLayoutProps) {
  return <AppShell>{children}</AppShell>;
}
