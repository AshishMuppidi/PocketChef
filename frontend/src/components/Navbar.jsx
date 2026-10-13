import { useState } from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { Utensils, Menu, X, Camera, User, LogIn, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const [isOpen, setIsOpen] = useState(false);
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const mainLinks = [
    { name: 'Home', path: '/' },
    { name: 'Community Feed', path: '/community' },
    { name: 'Search Recipes', path: '/search' },
    { name: 'My Pantry', path: '/pantry' }
  ];

  const getNavLinkClass = ({ isActive }) =>
    `px-4 py-2 rounded-lg text-base font-medium transition-all ${
      isActive
        ? 'text-[#3F8F49] font-bold bg-[#E8F4E9]'
        : 'text-[#111111] hover:text-[#5AAE62] hover:bg-[#F5F6F5]'
    }`;

  const handleLogout = () => {
    logout();
    setIsOpen(false);
    navigate('/', { replace: true });
  };

  const userInitial = (user?.displayName || user?.username || 'U').charAt(0).toUpperCase();

  return (
    <header className="bg-white border-b border-[#D5D8D5] shadow-xs sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-20 items-center">
          {/* Brand Logo */}
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-10 h-10 rounded-full bg-[#E8F4E9] flex items-center justify-center text-[#3F8F49] group-hover:bg-[#5AAE62] group-hover:text-white transition-colors">
              <Utensils className="w-5 h-5" />
            </div>
            <span className="font-bold text-xl tracking-tight text-[#111111]">
              WhatsOn<span className="text-[#5AAE62]">MyPlate</span>
            </span>
          </Link>

          {/* Desktop Navigation Links - Centered */}
          <nav className="hidden md:flex items-center space-x-4 lg:space-x-8">
            {mainLinks.map((link) => (
              <NavLink
                key={link.path}
                to={link.path}
                className={getNavLinkClass}
              >
                {link.name}
              </NavLink>
            ))}
          </nav>

          {/* Right Action Area */}
          <div className="hidden md:flex items-center space-x-3">
            <Link
              to="/scan"
              className="inline-flex items-center gap-2 bg-[#5AAE62] hover:bg-[#3F8F49] text-white px-4 py-2.5 rounded-lg text-sm font-semibold shadow-xs transition-colors"
            >
              <Camera className="w-4 h-4" />
              <span>Scan Dish</span>
            </Link>

            {isAuthenticated ? (
              <div className="flex items-center gap-2">
                <Link
                  to="/profile"
                  className="w-10 h-10 rounded-full bg-[#E8F4E9] border border-[#5AAE62]/40 flex items-center justify-center text-[#3F8F49] font-bold text-sm hover:ring-2 hover:ring-[#5AAE62]/30 transition-all"
                  aria-label="User Profile"
                  title={user?.displayName || user?.username}
                >
                  {userInitial}
                </Link>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="p-2.5 rounded-lg text-[#555555] hover:text-red-600 hover:bg-red-50 transition-colors"
                  aria-label="Sign out"
                  title="Sign out"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <Link
                to="/login"
                className="inline-flex items-center gap-1.5 border border-[#D5D8D5] hover:border-[#5AAE62] text-[#111111] hover:text-[#5AAE62] px-3.5 py-2 rounded-lg text-sm font-semibold transition-colors"
              >
                <LogIn className="w-4 h-4" />
                <span>Sign In</span>
              </Link>
            )}
          </div>

          {/* Mobile Menu Button */}
          <div className="flex md:hidden items-center">
            <button
              onClick={() => setIsOpen(!isOpen)}
              type="button"
              className="p-2 rounded-lg text-[#555555] hover:text-[#111111] hover:bg-[#F5F6F5] focus:outline-none"
              aria-label="Toggle navigation menu"
            >
              {isOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu Dropdown */}
      {isOpen && (
        <div className="md:hidden border-t border-[#D5D8D5] bg-white px-4 pt-3 pb-6 space-y-2">
          {mainLinks.map((link) => (
            <NavLink
              key={link.path}
              to={link.path}
              onClick={() => setIsOpen(false)}
              className={({ isActive }) =>
                `block px-4 py-2.5 rounded-lg text-base font-medium ${
                  isActive
                    ? 'text-[#3F8F49] font-bold bg-[#E8F4E9]'
                    : 'text-[#111111] hover:text-[#5AAE62] hover:bg-[#F5F6F5]'
                }`
              }
            >
              {link.name}
            </NavLink>
          ))}
          <div className="pt-2 border-t border-[#D5D8D5] flex flex-col gap-2">
            <Link
              to="/scan"
              onClick={() => setIsOpen(false)}
              className="flex items-center justify-center gap-2 bg-[#5AAE62] text-white px-4 py-2.5 rounded-lg font-semibold text-center"
            >
              <Camera className="w-4 h-4" />
              <span>Scan Dish</span>
            </Link>
            {isAuthenticated ? (
              <>
                <Link
                  to="/profile"
                  onClick={() => setIsOpen(false)}
                  className="flex items-center justify-center gap-2 border border-[#D5D8D5] text-[#111111] px-4 py-2.5 rounded-lg font-medium text-center"
                >
                  <User className="w-4 h-4" />
                  <span>My Profile ({user?.displayName || user?.username})</span>
                </Link>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="flex items-center justify-center gap-2 border border-red-200 text-red-600 bg-red-50 px-4 py-2.5 rounded-lg font-medium text-center"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Sign Out</span>
                </button>
              </>
            ) : (
              <Link
                to="/login"
                onClick={() => setIsOpen(false)}
                className="flex items-center justify-center gap-2 border border-[#D5D8D5] text-[#111111] px-4 py-2.5 rounded-lg font-medium text-center"
              >
                <LogIn className="w-4 h-4" />
                <span>Sign In</span>
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
