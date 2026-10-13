import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Utensils, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getApiErrorMessage } from '../utils/apiError';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Spinner from '../components/common/Spinner';

export default function Register() {
  const [username, setUsername] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setErrorMessage(null);

    // Client-side quick checks
    if (password.length < 8) {
      setErrorMessage('Password must be at least 8 characters long.');
      return;
    }

    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match. Please try again.');
      return;
    }

    setIsSubmitting(true);

    try {
      await register(
        username.trim(),
        email.trim(),
        password,
        displayName.trim() || username.trim()
      );
      navigate('/scan', { replace: true });
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Failed to create account. Please check your inputs.');
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
            Join WhatsOnMyPlate
          </h1>
          <p className="text-sm text-[#555555] mt-1">
            Create an account to start scanning and cooking.
          </p>
        </div>

        {errorMessage && (
          <div className="mb-6 p-3.5 bg-red-50 border border-red-200 rounded-xl flex items-start gap-2.5 text-xs text-red-700">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-4">
          <Input
            id="register-username"
            type="text"
            label="Username"
            icon={User}
            required
            placeholder="ChefMaria"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            disabled={isSubmitting}
          />

          <Input
            id="register-displayname"
            type="text"
            label="Display Name"
            icon={User}
            placeholder="Maria Chef"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            disabled={isSubmitting}
          />

          <Input
            id="register-email"
            type="email"
            label="Email Address"
            icon={Mail}
            required
            placeholder="maria@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isSubmitting}
          />

          <Input
            id="register-password"
            type="password"
            label="Password"
            icon={Lock}
            required
            placeholder="At least 8 characters"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isSubmitting}
          />

          <Input
            id="register-confirm"
            type="password"
            label="Confirm Password"
            icon={Lock}
            required
            placeholder="Re-enter your password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={isSubmitting}
          />

          <Button
            type="submit"
            size="lg"
            disabled={isSubmitting || !username.trim() || !email.trim() || !password}
            className="w-full py-3 font-bold mt-2"
          >
            {isSubmitting ? (
              <span className="flex items-center gap-2">
                <Spinner size="sm" className="border-white" />
                Creating Account...
              </span>
            ) : (
              'Create Account'
            )}
          </Button>
        </form>

        <p className="text-center text-sm text-[#555555] mt-6">
          Already have an account?{' '}
          <Link to="/login" className="font-bold text-[#5AAE62] hover:text-[#3F8F49]">
            Sign In
          </Link>
        </p>
      </Card>
    </div>
  );
}
