import { Clock, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import FoodImage from './FoodImage';
import Badge from '../common/Badge';

export default function RecipeCard({
  id,
  title,
  description,
  imageUrl,
  prepTimeMinutes = 10,
  cookTimeMinutes = 15,
  isAiGenerated = true,
  className = ''
}) {
  const totalMinutes = (prepTimeMinutes || 0) + (cookTimeMinutes || 0) || 20;

  return (
    <Link
      to={`/recipe/${id}`}
      className={`group block bg-white border border-[#D5D8D5] rounded-2xl overflow-hidden shadow-xs hover:border-[#5AAE62] hover:shadow-md transition-all ${className}`}
    >
      <div className="relative aspect-4/3 overflow-hidden bg-[#F5F6F5]">
        <FoodImage
          src={imageUrl}
          alt={title}
          className="rounded-none border-none aspect-4/3"
        />
        {isAiGenerated && (
          <div className="absolute top-3 left-3">
            <Badge variant="green" className="bg-white/95 backdrop-blur-xs text-[#3F8F49] shadow-xs">
              <Sparkles className="w-3 h-3 mr-1 inline" />
              AI Generated
            </Badge>
          </div>
        )}
      </div>

      <div className="p-5">
        <h3 className="text-base font-bold text-[#111111] group-hover:text-[#5AAE62] transition-colors line-clamp-1 mb-1.5">
          {title}
        </h3>
        {description && (
          <p className="text-xs text-[#555555] line-clamp-2 mb-4 leading-relaxed">
            {description}
          </p>
        )}

        <div className="flex items-center justify-between pt-3 border-t border-[#D5D8D5]/60 text-xs text-[#555555]">
          <div className="flex items-center gap-1.5 font-medium">
            <Clock className="w-3.5 h-3.5 text-[#5AAE62]" />
            <span>{totalMinutes} mins</span>
          </div>
          <span className="font-semibold text-[#5AAE62] group-hover:underline">
            View Recipe &rarr;
          </span>
        </div>
      </div>
    </Link>
  );
}
