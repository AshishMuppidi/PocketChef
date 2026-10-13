import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import RootLayout from './layouts/RootLayout';
import ProtectedRoute from './components/auth/ProtectedRoute';
import Home from './pages/Home';
import ScanDish from './pages/ScanDish';
import RecipeDetails from './pages/RecipeDetails';
import SearchRecipes from './pages/SearchRecipes';
import MyPantry from './pages/MyPantry';
import CommunityFeed from './pages/CommunityFeed';
import CreateCommunityPost from './pages/CreateCommunityPost';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<RootLayout />}>
          {/* Public Routes */}
          <Route index element={<Home />} />
          <Route path="search" element={<SearchRecipes />} />
          <Route path="community" element={<CommunityFeed />} />
          <Route path="recipe/:id" element={<RecipeDetails />} />
          <Route path="login" element={<Login />} />
          <Route path="register" element={<Register />} />
          <Route path="404" element={<NotFound />} />

          {/* Protected Routes */}
          <Route
            path="scan"
            element={
              <ProtectedRoute>
                <ScanDish />
              </ProtectedRoute>
            }
          />
          <Route
            path="pantry"
            element={
              <ProtectedRoute>
                <MyPantry />
              </ProtectedRoute>
            }
          />
          <Route
            path="community/create"
            element={
              <ProtectedRoute>
                <CreateCommunityPost />
              </ProtectedRoute>
            }
          />
          <Route
            path="profile"
            element={
              <ProtectedRoute>
                <Profile />
              </ProtectedRoute>
            }
          />

          {/* Fallback 404 Route */}
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
