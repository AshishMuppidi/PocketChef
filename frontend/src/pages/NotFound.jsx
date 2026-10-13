import { Link } from 'react-router-dom';
import { UtensilsCrossed, Home } from 'lucide-react';
import Button from '../components/common/Button';

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[calc(100vh-260px)] text-center py-12">
      <div className="w-16 h-16 rounded-full bg-[#E8F4E9] flex items-center justify-center text-[#3F8F49] mb-4">
        <UtensilsCrossed className="w-8 h-8" />
      </div>
      <h1 className="text-3xl font-extrabold text-[#111111] mb-2 tracking-tight">
        Page Not Found
      </h1>
      <p className="text-sm text-[#555555] max-w-sm mb-6">
        Sorry, the page or dish recipe you're looking for doesn't exist or has been moved.
      </p>
      <Link to="/">
        <Button icon={Home} size="md">
          Return Home
        </Button>
      </Link>
    </div>
  );
}
