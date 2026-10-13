import { Navigate, useLocation, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Spinner from '../common/Spinner';

export default function ProtectedRoute({ children }) {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] py-12">
        <Spinner size="lg" />
        <span className="mt-4 text-sm font-medium text-[#555555]">
          Verifying authentication...
        </span>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Redirect to login preserving destination in state
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children ? children : <Outlet />;
}
