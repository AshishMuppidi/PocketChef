import { Utensils, Heart } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-white border-t border-[#D5D8D5] py-10 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-[#E8F4E9] flex items-center justify-center text-[#3F8F49]">
              <Utensils className="w-4 h-4" />
            </div>
            <span className="font-bold text-lg text-[#111111]">
              WhatsOn<span className="text-[#5AAE62]">MyPlate</span>
            </span>
          </div>

          <div className="flex flex-wrap justify-center gap-6 text-sm text-[#555555]">
            <Link to="/" className="hover:text-[#5AAE62] transition-colors">Home</Link>
            <Link to="/scan" className="hover:text-[#5AAE62] transition-colors">Scan Dish</Link>
            <Link to="/search" className="hover:text-[#5AAE62] transition-colors">Search Recipes</Link>
            <Link to="/pantry" className="hover:text-[#5AAE62] transition-colors">My Pantry</Link>
            <Link to="/community" className="hover:text-[#5AAE62] transition-colors">Community Feed</Link>
          </div>

          <p className="text-xs text-[#555555] flex items-center gap-1">
            Made with <Heart className="w-3.5 h-3.5 text-red-500 fill-red-500" /> for home cooks everywhere.
          </p>
        </div>
      </div>
    </footer>
  );
}
