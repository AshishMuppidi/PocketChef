import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, Clock, Users, Sparkles, CheckCircle2, AlertCircle, Info } from 'lucide-react';
import apiClient from '../api/apiClient';
import { useAuth } from '../context/AuthContext';
import { mockRecipes } from '../data/mockRecipes';
import { getApiErrorMessage } from '../utils/apiError';
import FoodImage from '../components/recipe/FoodImage';
import SpiceLevelSelector from '../components/recipe/SpiceLevelSelector';
import SmartSubstitution from '../components/recipe/SmartSubstitution';
import Badge from '../components/common/Badge';
import Spinner from '../components/common/Spinner';
import EmptyState from '../components/common/EmptyState';

export default function RecipeDetails() {
  const { id } = useParams();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [recipe, setRecipe] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState(null);

  // Spice level adjustment states
  const [currentSpiceLevel, setCurrentSpiceLevel] = useState('MEDIUM');
  const [isAdjustingSpice, setIsAdjustingSpice] = useState(false);
  const [spiceAdjustmentError, setSpiceAdjustmentError] = useState(null);
  const [adjustedIngredientsMap, setAdjustedIngredientsMap] = useState({});
  const [adjustmentNotes, setAdjustmentNotes] = useState(null);

  const [checkedIngredients, setCheckedIngredients] = useState({});

  useEffect(() => {
    let isMounted = true;

    async function fetchRecipe() {
      setIsLoading(true);
      setErrorMessage(null);

      // Attempt to load from real backend API first
      try {
        const response = await apiClient.get(`/recipes/${id}`);
        if (isMounted && response.data) {
          setRecipe(response.data);
          setCurrentSpiceLevel(response.data.spiceLevel || 'MEDIUM');
          setAdjustedIngredientsMap({});
          setAdjustmentNotes(null);
          setIsLoading(false);
          return;
        }
      } catch (err) {
        // If not found in backend or if offline, check if matching mock recipe exists as fallback
        const mockFallback = mockRecipes.find((r) => r.id === id);
        if (isMounted) {
          if (mockFallback) {
            setRecipe(mockFallback);
            setCurrentSpiceLevel(mockFallback.spiceLevel || 'MEDIUM');
          } else {
            const errorMsg = err.response?.status === 404
              ? 'The requested recipe could not be found.'
              : getApiErrorMessage(err, 'Failed to load recipe details.');
            setErrorMessage(errorMsg);
          }
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    if (id) {
      fetchRecipe();
    }

    return () => {
      isMounted = false;
    };
  }, [id]);

  const handleSpiceLevelChange = async (newLevel) => {
    if (newLevel === currentSpiceLevel) return;

    // If reverting back to original recipe spice level
    if (recipe && recipe.spiceLevel === newLevel) {
      setCurrentSpiceLevel(newLevel);
      setAdjustedIngredientsMap({});
      setAdjustmentNotes(null);
      setSpiceAdjustmentError(null);
      return;
    }

    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    setIsAdjustingSpice(true);
    setSpiceAdjustmentError(null);

    try {
      const response = await apiClient.post(`/recipes/${recipe.id}/spice-adjustment`, {
        targetSpiceLevel: newLevel
      });

      const data = response.data;
      setCurrentSpiceLevel(newLevel);

      // Build map of adjusted ingredients keyed by normalized name
      const map = {};
      if (Array.isArray(data.adjustedIngredients)) {
        data.adjustedIngredients.forEach((adj) => {
          map[adj.name.toLowerCase().trim()] = adj;
        });
      }
      setAdjustedIngredientsMap(map);
      setAdjustmentNotes(data.notes || null);
    } catch (err) {
      setSpiceAdjustmentError('Unable to adjust spice level right now. Your original recipe is unchanged.');
    } finally {
      setIsAdjustingSpice(false);
    }
  };

  const toggleIngredient = (idx) => {
    setCheckedIngredients((prev) => ({
      ...prev,
      [idx]: !prev[idx]
    }));
  };

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] py-12">
        <Spinner size="lg" />
        <span className="mt-4 text-sm font-semibold text-[#555555]">
          Loading recipe details...
        </span>
      </div>
    );
  }

  if (errorMessage && !recipe) {
    return (
      <div className="max-w-2xl mx-auto py-12">
        <EmptyState
          title="Recipe Not Found"
          description={errorMessage}
          actionLabel="Back to Recipes"
          onAction={() => window.history.back()}
        />
      </div>
    );
  }

  if (!recipe) return null;

  const totalMinutes = ((recipe.prepTimeMinutes || 0) + (recipe.cookTimeMinutes || 0)) || 25;
  const ingredientsList = Array.isArray(recipe.ingredients) ? recipe.ingredients : [];
  const instructionsList = Array.isArray(recipe.instructions) ? recipe.instructions : [];

  return (
    <div className="max-w-5xl mx-auto space-y-8 pb-12">
      {/* Back Navigation */}
      <div>
        <Link
          to="/search"
          className="inline-flex items-center gap-1.5 text-sm font-semibold text-[#555555] hover:text-[#5AAE62] transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to recipes</span>
        </Link>
      </div>

      {/* Main Title Header */}
      <div>
        <div className="flex flex-wrap items-center gap-3 mb-2">
          {recipe.isAiGenerated && (
            <Badge variant="green" className="py-1 px-3 text-xs">
              <Sparkles className="w-3.5 h-3.5 mr-1.5 inline" />
              AI Generated
            </Badge>
          )}
          <div className="flex items-center gap-4 text-xs font-semibold text-[#555555]">
            <span className="flex items-center gap-1">
              <Clock className="w-3.5 h-3.5 text-[#5AAE62]" />
              {totalMinutes} mins
            </span>
            <span className="flex items-center gap-1">
              <Users className="w-3.5 h-3.5 text-[#5AAE62]" />
              {recipe.servings || 2} servings
            </span>
          </div>
        </div>

        <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-[#111111] tracking-tight">
          {recipe.title}
        </h1>
        {recipe.description && (
          <p className="mt-2 text-sm sm:text-base text-[#555555] max-w-3xl leading-relaxed">
            {recipe.description}
          </p>
        )}
      </div>

      {/* Non-blocking Spice Adjustment Error Alert */}
      {spiceAdjustmentError && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl flex items-center gap-3 text-sm text-amber-800">
          <AlertCircle className="w-5 h-5 shrink-0 text-amber-600" />
          <span>{spiceAdjustmentError}</span>
        </div>
      )}

      {/* Top Section: Food Image + Spice Level Control Card */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-start">
        {/* Main Food Image (Cloudinary URL or fallback) */}
        <div className="md:col-span-7 lg:col-span-7">
          <FoodImage
            src={recipe.imageUrl}
            alt={recipe.title}
            aspectRatio="aspect-16/10"
            className="w-full shadow-xs"
          />
        </div>

        {/* Spice Level Control Card */}
        <div className="md:col-span-5 lg:col-span-5 flex flex-col gap-6">
          <SpiceLevelSelector
            value={currentSpiceLevel}
            isLoading={isAdjustingSpice}
            onChange={handleSpiceLevelChange}
          />

          <div className="bg-[#F5F6F5] border border-[#D5D8D5] rounded-2xl p-5 text-xs text-[#555555] leading-relaxed">
            <h4 className="font-bold text-[#111111] mb-1 text-sm flex items-center gap-1.5">
              <Sparkles className="w-4 h-4 text-[#5AAE62]" />
              Chef's AI Note
            </h4>
            {adjustmentNotes ? (
              <p className="text-[#333333] font-medium mt-1">
                {adjustmentNotes}
              </p>
            ) : (
              <p>
                Recipe ingredients and instruction steps are precision-analyzed by Google Gemini 2.5 Flash and stored securely in your catalog.
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Two-Column Content: Ingredients & Substitution (LEFT) | Instructions (RIGHT) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 pt-4">
        {/* LEFT Column: Ingredients (lg: 5 cols) */}
        <div className="lg:col-span-5 space-y-6">
          <div className="bg-white border border-[#D5D8D5] rounded-2xl p-6 shadow-xs">
            <div className="flex items-center justify-between pb-3 border-b border-[#D5D8D5] mb-4">
              <h2 className="text-xl font-extrabold text-[#111111]">
                Ingredients
              </h2>
              <div className="flex items-center gap-2">
                {Object.keys(adjustedIngredientsMap).length > 0 && (
                  <span className="text-[11px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-800">
                    Spice Adjusted ({currentSpiceLevel})
                  </span>
                )}
                <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-[#E8F4E9] text-[#3F8F49]">
                  {ingredientsList.length} items
                </span>
              </div>
            </div>

            <ul className="space-y-3">
              {ingredientsList.map((item, idx) => {
                const isChecked = !!checkedIngredients[idx];
                const normalizedName = (item.name || '').toLowerCase().trim();

                // Check if this ingredient was adjusted by AI for spice level
                const adjustedItem = adjustedIngredientsMap[normalizedName] ||
                  Object.values(adjustedIngredientsMap).find(adj =>
                    normalizedName.includes(adj.name.toLowerCase().trim()) ||
                    adj.name.toLowerCase().trim().includes(normalizedName)
                  );

                const origQuantityStr = item.quantity ? `${item.quantity}` : '';
                const origUnitStr = item.unit ? ` ${item.unit}` : '';
                const displayOrigQty = `${origQuantityStr}${origUnitStr}`.trim();

                return (
                  <li
                    key={idx}
                    onClick={() => toggleIngredient(idx)}
                    className="p-2.5 rounded-xl hover:bg-[#F5F6F5] cursor-pointer transition-colors"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-start gap-3">
                        <div
                          className={`w-5 h-5 rounded-md border flex items-center justify-center shrink-0 mt-0.5 transition-colors ${
                            isChecked
                              ? 'bg-[#5AAE62] border-[#5AAE62] text-white'
                              : 'border-[#D5D8D5] bg-white'
                          }`}
                        >
                          {isChecked && <CheckCircle2 className="w-3.5 h-3.5" />}
                        </div>
                        <div>
                          <span
                            className={`text-sm md:text-base font-medium ${
                              isChecked ? 'line-through text-[#888888]' : 'text-[#111111]'
                            }`}
                          >
                            {adjustedItem ? (
                              <>
                                <strong className="text-[#3F8F49]">
                                  {adjustedItem.adjustedQuantity} {adjustedItem.adjustedUnit || ''}{' '}
                                </strong>
                                <span className="line-through text-xs text-[#888888] mr-1">
                                  ({displayOrigQty})
                                </span>
                              </>
                            ) : (
                              displayOrigQty ? <strong>{displayOrigQty} </strong> : ''
                            )}
                            {item.name}
                          </span>

                          {adjustedItem?.changeReason && (
                            <p className="text-[11px] text-[#555555] italic mt-0.5">
                              {adjustedItem.changeReason}
                            </p>
                          )}
                        </div>
                      </div>
                    </div>
                  </li>
                );
              })}
            </ul>
          </div>

          {/* Smart Substitution Card */}
          <SmartSubstitution
            recipeId={recipe.id}
            ingredients={ingredientsList}
          />
        </div>

        {/* RIGHT Column: Step-by-Step Instructions (lg: 7 cols) */}
        <div className="lg:col-span-7">
          <div className="bg-white border border-[#D5D8D5] rounded-2xl p-6 sm:p-8 shadow-xs">
            <div className="flex items-center justify-between pb-3 border-b border-[#D5D8D5] mb-6">
              <h2 className="text-xl font-extrabold text-[#111111]">
                Step-by-Step Instructions
              </h2>
              <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-[#E8F4E9] text-[#3F8F49]">
                {instructionsList.length} steps
              </span>
            </div>

            <ol className="space-y-5">
              {instructionsList.map((inst, idx) => (
                <li key={inst.step || idx} className="flex gap-4">
                  <div className="w-8 h-8 rounded-full bg-[#E8F4E9] text-[#3F8F49] font-bold text-sm flex items-center justify-center shrink-0 border border-[#5AAE62]/30 shadow-xs">
                    {inst.step || idx + 1}
                  </div>
                  <div className="pt-0.5">
                    <p className="text-sm md:text-base text-[#111111] leading-relaxed">
                      {inst.text}
                    </p>
                  </div>
                </li>
              ))}
            </ol>
          </div>
        </div>
      </div>
    </div>
  );
}
