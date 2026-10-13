import { Star, StarHalf } from 'lucide-react';

export default function RatingStars({
  rating = 5.0,
  maxStars = 5,
  size = 18,
  showScore = true,
  className = ''
}) {
  const fullStars = Math.floor(rating);
  const hasHalfStar = rating % 1 >= 0.4 && rating % 1 <= 0.8;
  const emptyStars = Math.max(0, maxStars - fullStars - (hasHalfStar ? 1 : 0));

  return (
    <div className={`flex items-center gap-1.5 ${className}`}>
      <div className="flex items-center text-amber-400">
        {Array.from({ length: fullStars }).map((_, i) => (
          <Star key={`full-${i}`} size={size} className="fill-amber-400 text-amber-400" />
        ))}
        {hasHalfStar && (
          <StarHalf size={size} className="fill-amber-400 text-amber-400" />
        )}
        {Array.from({ length: emptyStars }).map((_, i) => (
          <Star key={`empty-${i}`} size={size} className="text-gray-300" />
        ))}
      </div>
      {showScore && (
        <span className="text-sm font-semibold text-[#111111]">
          {rating.toFixed(1)}/5
        </span>
      )}
    </div>
  );
}
