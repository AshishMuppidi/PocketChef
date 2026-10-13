import { useState, useEffect } from 'react';
import { Sparkles, ArrowRight, AlertCircle, CheckCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../api/apiClient';
import { useAuth } from '../../context/AuthContext';
import { getApiErrorMessage } from '../../utils/apiError';
import Button from '../common/Button';
import Spinner from '../common/Spinner';

export default function SmartSubstitution({
  recipeId,
  ingredients = [],
  className = ''
}) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // Extract ingredient names from recipe ingredients list
  const ingredientNames = Array.isArray(ingredients)
    ? ingredients.map((i) => (typeof i === 'string' ? i : i.name)).filter(Boolean)
    : [];

  const [selectedIngredient, setSelectedIngredient] = useState('');
  const [reason, setReason] = useState('');
  const [substitutionResult, setSubstitutionResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  // Set default selection when ingredients load
  useEffect(() => {
    if (ingredientNames.length > 0 && !selectedIngredient) {
      setSelectedIngredient(ingredientNames[0]);
    }
  }, [ingredientNames]);

  const handleFindSubstitute = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!selectedIngredient || !recipeId || isLoading) return;

    setIsLoading(true);
    setErrorMessage(null);
    setSubstitutionResult(null);

    try {
      const response = await apiClient.post(`/recipes/${recipeId}/substitutions`, {
        ingredientName: selectedIngredient,
        reason: reason.trim() || undefined
      });
      setSubstitutionResult(response.data);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Unable to find an AI substitute. Please try again.'));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={`bg-white border border-[#D5D8D5] rounded-2xl p-6 shadow-xs ${className}`}>
      <div className="flex items-center justify-between gap-2 mb-4">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-[#E8F4E9] flex items-center justify-center text-[#3F8F49]">
            <Sparkles className="w-4 h-4" />
          </div>
          <h3 className="text-lg font-bold text-[#111111]">Smart Substitution</h3>
        </div>
        <span className="text-[11px] font-bold px-2 py-0.5 rounded-full bg-[#F5F6F5] text-[#555555]">
          AI Powered
        </span>
      </div>

      <div className="space-y-4">
        {ingredientNames.length > 0 ? (
          <>
            <div>
              <label htmlFor="missing-ingredient-select" className="block text-xs font-semibold text-[#555555] mb-1.5">
                Select ingredient to replace:
              </label>
              <select
                id="missing-ingredient-select"
                value={selectedIngredient}
                onChange={(e) => {
                  setSelectedIngredient(e.target.value);
                  setSubstitutionResult(null);
                  setErrorMessage(null);
                }}
                disabled={isLoading}
                className="w-full bg-[#F5F6F5] border border-[#D5D8D5] rounded-xl px-3.5 py-2.5 text-sm font-semibold text-[#111111] focus:border-[#5AAE62] focus:ring-2 focus:ring-[#5AAE62]/20 focus:outline-none transition-colors cursor-pointer disabled:opacity-60"
              >
                {ingredientNames.map((name) => (
                  <option key={name} value={name}>
                    {name}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex gap-2">
              <Button
                type="button"
                size="sm"
                onClick={handleFindSubstitute}
                disabled={isLoading || !selectedIngredient}
                className="w-full justify-center"
              >
                {isLoading ? (
                  <span className="flex items-center gap-2">
                    <Spinner size="sm" className="border-white" />
                    Finding AI Substitute...
                  </span>
                ) : (
                  <span className="flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5" />
                    Find Substitute
                  </span>
                )}
              </Button>
            </div>
          </>
        ) : (
          <p className="text-xs text-[#888888] italic">No recipe ingredients available for substitution.</p>
        )}

        {/* Error Alert */}
        {errorMessage && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-xl flex items-center gap-2 text-xs text-red-700">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Dynamic AI Substitution Result Card */}
        {substitutionResult && (
          <div className="p-4 bg-[#E8F4E9]/60 border border-[#5AAE62]/40 rounded-xl space-y-2.5 animate-fadeIn">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-[#3F8F49]">
                AI Recommendation
              </span>
              {substitutionResult.confidence && (
                <span className="text-[11px] font-bold text-[#3F8F49] bg-white px-2 py-0.5 rounded-full border border-[#5AAE62]/30 shadow-2xs">
                  {Math.round(substitutionResult.confidence * 100)}% Confidence
                </span>
              )}
            </div>

            <div className="flex items-start gap-2 text-sm font-medium text-[#111111]">
              <ArrowRight className="w-4 h-4 text-[#5AAE62] shrink-0 mt-0.5" />
              <div>
                <span>Replace with: </span>
                <strong className="text-[#3F8F49] font-extrabold">{substitutionResult.substitute}</strong>
                {substitutionResult.quantity && (
                  <span className="block text-xs font-semibold text-[#555555] mt-0.5">
                    Ratio/Quantity: {substitutionResult.quantity}
                  </span>
                )}
              </div>
            </div>

            {substitutionResult.reason && (
              <p className="text-xs text-[#555555] leading-relaxed pt-1 border-t border-[#5AAE62]/20">
                <span className="font-semibold text-[#111111]">Why: </span>
                {substitutionResult.reason}
              </p>
            )}

            {substitutionResult.instructions && (
              <p className="text-xs text-[#555555] leading-relaxed">
                <span className="font-semibold text-[#111111]">Instructions: </span>
                {substitutionResult.instructions}
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
