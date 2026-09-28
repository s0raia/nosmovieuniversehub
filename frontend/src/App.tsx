import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell/AppShell';
import { AuthProvider } from './contexts/AuthContext';
import { StarredProvider } from './contexts/StarredContext';
import { CataloguePage } from './pages/CataloguePage';
import { HomePage } from './pages/HomePage';
import { AuthAccountPage } from './pages/AuthAccountPage';
import { MovieDetailPage } from './pages/MovieDetailPage';
import { PlaylistsPage } from './pages/PlaylistsPage';

export default function App() {
  return (
    <AuthProvider>
      <StarredProvider>
        <AppShell>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/catalogue" element={<CataloguePage />} />
            <Route path="/movies/:tmdbId" element={<MovieDetailPage />} />
            <Route path="/playlists" element={<PlaylistsPage />} />
            <Route path="/account" element={<AuthAccountPage />} />
            <Route path="/login" element={<AuthAccountPage />} />
            <Route path="/register" element={<AuthAccountPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AppShell>
      </StarredProvider>
    </AuthProvider>
  );
}
