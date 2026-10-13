import { useState } from 'react';
import { Utensils } from 'lucide-react';

export default function FoodImage({
  src,
  alt = 'Delicious food dish',
  className = '',
  aspectRatio = 'aspect-4/3'
}) {
  const [hasError, setHasError] = useState(false);

  const fallbackUrl = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=800&q=80';

  return (
    <div className={`relative overflow-hidden rounded-2xl bg-[#F5F6F5] border border-[#D5D8D5] ${aspectRatio} ${className}`}>
      {!hasError && src ? (
        <img
          src={src}
          alt={alt}
          onError={() => setHasError(true)}
          className="w-full h-full object-cover transition-transform duration-300 hover:scale-102"
          loading="lazy"
        />
      ) : (
        <div className="w-full h-full flex flex-col items-center justify-center text-[#555555] p-4 text-center">
          <Utensils className="w-10 h-10 text-[#5AAE62] mb-2" />
          <span className="text-xs font-medium">Food Photo</span>
        </div>
      )}
    </div>
  );
}
