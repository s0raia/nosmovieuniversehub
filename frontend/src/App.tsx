import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { MyPlaylistsProvider } from './contexts/MyPlaylistsContext';
import { StarredProvider } from './contexts/StarredContext';
import { MainLayout } from './layouts/MainLayout';
import { AuthAccountPage } from './pages/Account/AuthAccountPage';
import { CataloguePage } from './pages/Catalogue/CataloguePage';
import { HomePage } from './pages/Home/HomePage';
import { MovieDetailPage } from './pages/MovieDetail/MovieDetailPage';
import { ComparePlaylistsPage } from './pages/Compare/ComparePlaylistsPage';
import { PlaylistsPage } from './pages/Playlists/PlaylistsPage';

export default function App() {
  return (
    <AuthProvider>
      <MyPlaylistsProvider>
        <StarredProvider>
          <MainLayout>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/catalogue" element={<CataloguePage />} />
            <Route path="/movies/:tmdbId" element={<MovieDetailPage />} />
            <Route path="/playlists" element={<PlaylistsPage />} />
            <Route path="/compare" element={<ComparePlaylistsPage />} />
            <Route path="/account" element={<AuthAccountPage />} />
            <Route path="/login" element={<AuthAccountPage />} />
            <Route path="/register" element={<AuthAccountPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
          </MainLayout>
        </StarredProvider>
      </MyPlaylistsProvider>
    </AuthProvider>
  );
}
