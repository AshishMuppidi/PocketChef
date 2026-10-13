import { Clock, Sparkles, Check } from 'lucide-react';
import { Link } from 'react-router-dom';
import FoodImage from '../recipe/FoodImage';
import Badge from '../common/Badge';

export default function PantryRecipeCard({
  id,
  title,
  time = '20 mins',
  isAiGenerated = true,
  imageUrl,
  matchedIngredients,
  totalIngredients,
  matchPercentage,
  className = ''
}) {
  return (
    <Link
      to={`/recipe/${id}`}
      className={`group block bg-white border border-[#D5D8D5] rounded-2xl overflow-hidden shadow-2xs hover:border-[#5AAE62] hover:shadow-xs transition-all ${className}`}
    >
      <div className="relative aspect-16/10 overflow-hidden bg-[#F5F6F5]">
        <FoodImage
          src={imageUrl}
          alt={title}
          className="rounded-none border-none aspect-16/10"
        />
        {isAiGenerated && (
          <div className="absolute top-2.5 left-2.5">
            <Badge variant="green" className="bg-white/95 backdrop-blur-xs text-[#3F8F49] text-[11px] shadow-2xs">
              <Sparkles className="w-3 h-3 mr-1 inline" />
              AI Generated
            </Badge>
          </div>
        )}
        {matchedIngredients !== undefined && totalIngredients !== undefined && (
          <div className="absolute bottom-2.5 right-2.5">
            <span className="inline-flex items-center gap-1 bg-[#111111]/85 backdrop-blur-xs text-white text-[11px] font-bold px-2 py-0.5 rounded-full shadow-2xs">
              <Check className="w-3 h-3 text-[#5AAE62]" />
              {matchedIngredients}/{totalIngredients} ingredients
            </span>
          </div>
        )}
      </div>

      <div className="p-4">
        <h4 className="font-bold text-sm md:text-base text-[#111111] group-hover:text-[#5AAE62] transition-colors line-clamp-1 mb-1">
          {title}
        </h4>
        <div className="flex items-center justify-between text-xs text-[#555555]">
          <div className="flex items-center gap-1">
            <Clock className="w-3.5 h-3.5 text-[#5AAE62]" />
            <span>{time}</span>
          </div>
          {matchPercentage !== undefined && (
            <span className="font-semibold text-[#3F8F49]">
              {matchPercentage}% match
            </span>
          )}
        </div>
      </div>
    </Link>
  );
}
