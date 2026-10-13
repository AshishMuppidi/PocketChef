import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Mail, Lock, Utensils, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getApiErrorMessage } from '../utils/apiError';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Spinner from '../components/common/Spinner';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const redirectPath = location.state?.from?.pathname || '/scan';

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorMessage(null);
    setIsSubmitting(true);

    try {
      await login(email.trim(), password);
      navigate(redirectPath, { replace: true });
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Invalid email or password.');
      setErrorMessage(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-[calc(100vh-240px)] py-6">
      <Card className="w-full max-w-md p-8 sm:p-10 shadow-xs">
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-full bg-[#E8F4E9] flex items-center justify-center text-[#3F8F49] mx-auto mb-3">
            <Utensils className="w-6 h-6" />
          </div>
          <h1 className="text-2xl font-extrabold text-[#111111] tracking-tight">
            Welcome Back
          </h1>
          <p className="text-sm text-[#555555] mt-1">
            Sign in to access your saved recipes and pantry.
          </p>
        </div>

        {errorMessage && (
          <div className="mb-6 p-3.5 bg-red-50 border border-red-200 rounded-xl flex items-start gap-2.5 text-xs text-red-700">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-4">
          <Input
            id="login-email"
            type="email"
            label="Email Address"
            icon={Mail}
            required
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isSubmitting}
          />

          <Input
            id="login-password"
            type="password"
            label="Password"
            icon={Lock}
            required
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isSubmitting}
          />

          <Button
            type="submit"
            size="lg"
            disabled={isSubmitting || !email.trim() || !password}
            className="w-full py-3 font-bold mt-2"
          >
            {isSubmitting ? (
              <span className="flex items-center gap-2">
                <Spinner size="sm" className="border-white" />
                Signing In...
              </span>
            ) : (
              'Sign In'
            )}
          </Button>
        </form>

        <p className="text-center text-sm text-[#555555] mt-6">
          Don't have an account?{' '}
          <Link to="/register" className="font-bold text-[#5AAE62] hover:text-[#3F8F49]">
            Create Account
          </Link>
        </p>
      </Card>
    </div>
  );
}
