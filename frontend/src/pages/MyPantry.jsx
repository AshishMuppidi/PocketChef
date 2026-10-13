import { useState, useEffect } from 'react';
import { Search, Plus, Refrigerator, Sparkles, AlertCircle, RefreshCw } from 'lucide-react';
import apiClient from '../api/apiClient';
import { getApiErrorMessage } from '../utils/apiError';
import PantryItemRow from '../components/pantry/PantryItemRow';
import PantryRecipeCard from '../components/pantry/PantryRecipeCard';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';
import EmptyState from '../components/common/EmptyState';

export default function MyPantry() {
  const [searchQuery, setSearchQuery] = useState('');
  const [pantryItems, setPantryItems] = useState([]);
  const [pantryRecipes, setPantryRecipes] = useState([]);

  // Form states
  const [newItemName, setNewItemName] = useState('');
  const [newItemQty, setNewItemQty] = useState('');
  const [newItemUnit, setNewItemUnit] = useState('');

  const [isLoading, setIsLoading] = useState(true);
  const [isAdding, setIsAdding] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  // Load pantry inventory and matching recipes concurrently
  const loadPantryData = async () => {
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const [itemsRes, recipesRes] = await Promise.all([
        apiClient.get('/pantry'),
        apiClient.get('/pantry/recipes')
      ]);

      setPantryItems(itemsRes.data || []);
      setPantryRecipes(recipesRes.data || []);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Unable to load your pantry. Please try again.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadPantryData();
  }, []);

  const handleAddItem = async (e) => {
    e.preventDefault();
    if (!newItemName.trim() || isAdding) return;

    setIsAdding(true);
    setErrorMessage(null);

    try {
      const payload = {
        name: newItemName.trim(),
        quantity: newItemQty ? parseFloat(newItemQty) : null,
        unit: newItemUnit.trim() || null
      };

      const response = await apiClient.post('/pantry', payload);
      const savedItem = response.data;

      // Update pantry items state
      setPantryItems((prev) => {
        const existingIdx = prev.findIndex((item) => item.id === savedItem.id);
        if (existingIdx >= 0) {
          const updated = [...prev];
          updated[existingIdx] = savedItem;
          return updated;
        }
        return [...prev, savedItem].sort((a, b) => a.name.localeCompare(b.name));
      });

      // Clear input fields
      setNewItemName('');
      setNewItemQty('');
      setNewItemUnit('');

      // Refresh recipe matches based on updated inventory
      try {
        const recipesRes = await apiClient.get('/pantry/recipes');
        setPantryRecipes(recipesRes.data || []);
      } catch {
        // Soft-fail matching refresh
      }
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Failed to add ingredient to pantry.'));
    } finally {
      setIsAdding(false);
    }
  };

  const handleRemoveItem = async (id) => {
    try {
      await apiClient.delete(`/pantry/${id}`);
      setPantryItems((prev) => prev.filter((item) => item.id !== id));

      // Refresh recipe matches
      try {
        const recipesRes = await apiClient.get('/pantry/recipes');
        setPantryRecipes(recipesRes.data || []);
      } catch {
        // Soft-fail matching refresh
      }
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Failed to remove pantry item.'));
    }
  };

  // Filter matching recipes locally by title search query
  const filteredRecipes = pantryRecipes.filter((item) => {
    const title = item.recipe?.title || '';
    return title.toLowerCase().includes(searchQuery.toLowerCase());
  });

  return (
    <div className="space-y-10 pb-12">
      {/* Top Centered Search Field */}
      <div className="flex flex-col items-center justify-center max-w-[680px] mx-auto text-center">
        <h1 className="text-2xl sm:text-3xl font-extrabold text-[#111111] mb-2 tracking-tight">
          My Pantry
        </h1>
        <p className="text-sm text-[#555555] mb-6">
          Track ingredients in stock and discover what you can cook immediately.
        </p>

        <div className="w-full">
          <Input
            id="pantry-search"
            icon={Search}
            placeholder="Search for dishes (e.g., lasagna)"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="py-3.5 text-base rounded-xl shadow-xs"
          />
        </div>
      </div>

      {/* Error Alert */}
      {errorMessage && (
        <div className="max-w-4xl mx-auto p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center justify-between gap-3 text-sm text-red-700">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span>{errorMessage}</span>
          </div>
          <Button
            size="sm"
            variant="secondary"
            icon={RefreshCw}
            onClick={loadPantryData}
          >
            Retry
          </Button>
        </div>
      )}

      {/* Two-Column Layout: Left (Inventory) | Right (Matching Recipes) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* LEFT Column: My Pantry Inventory (5 cols) */}
        <div className="lg:col-span-5 bg-white border border-[#D5D8D5] rounded-2xl p-6 shadow-xs space-y-6">
          <div className="flex items-center justify-between pb-3 border-b border-[#D5D8D5]">
            <div className="flex items-center gap-2">
              <Refrigerator className="w-5 h-5 text-[#5AAE62]" />
              <h2 className="text-lg font-bold text-[#111111]">
                My Pantry Inventory
              </h2>
            </div>
            <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-[#E8F4E9] text-[#3F8F49]">
              {pantryItems.length} items
            </span>
          </div>

          {/* Quick Add Form */}
          <form onSubmit={handleAddItem} className="space-y-2">
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Ingredient name (e.g., Rice)..."
                required
                value={newItemName}
                onChange={(e) => setNewItemName(e.target.value)}
                disabled={isAdding}
                className="flex-1 bg-[#F5F6F5] border border-[#D5D8D5] rounded-lg px-3 py-2 text-sm text-[#111111] focus:border-[#5AAE62] focus:ring-1 focus:ring-[#5AAE62] focus:outline-none"
              />
              <Button
                type="submit"
                size="sm"
                icon={isAdding ? undefined : Plus}
                disabled={isAdding || !newItemName.trim()}
              >
                {isAdding ? <Spinner size="sm" className="border-white" /> : 'Add'}
              </Button>
            </div>
            <div className="grid grid-cols-2 gap-2">
              <input
                type="number"
                step="any"
                min="0.01"
                placeholder="Qty (e.g. 2)"
                value={newItemQty}
                onChange={(e) => setNewItemQty(e.target.value)}
                disabled={isAdding}
                className="bg-[#F5F6F5] border border-[#D5D8D5] rounded-lg px-3 py-1.5 text-xs text-[#111111] focus:border-[#5AAE62] focus:ring-1 focus:ring-[#5AAE62] focus:outline-none"
              />
              <input
                type="text"
                placeholder="Unit (e.g. cups, kg)"
                value={newItemUnit}
                onChange={(e) => setNewItemUnit(e.target.value)}
                disabled={isAdding}
                className="bg-[#F5F6F5] border border-[#D5D8D5] rounded-lg px-3 py-1.5 text-xs text-[#111111] focus:border-[#5AAE62] focus:ring-1 focus:ring-[#5AAE62] focus:outline-none"
              />
            </div>
          </form>

          {/* Item Rows */}
          {isLoading ? (
            <div className="flex flex-col items-center justify-center py-8">
              <Spinner size="md" />
              <span className="mt-2 text-xs font-semibold text-[#888888]">
                Loading inventory...
              </span>
            </div>
          ) : (
            <div className="space-y-2.5 max-h-[520px] overflow-y-auto pr-1">
              {pantryItems.length > 0 ? (
                pantryItems.map((item) => (
                  <PantryItemRow
                    key={item.id}
                    item={item}
                    onRemove={handleRemoveItem}
                  />
                ))
              ) : (
                <EmptyState
                  icon={Refrigerator}
                  title="Your pantry is empty"
                  description="Add ingredients you have at home above to discover recipes you can cook."
                />
              )}
            </div>
          )}
        </div>

        {/* RIGHT Column: Recipes you can cook now (7 cols) */}
        <div className="lg:col-span-7 space-y-4">
          <div className="flex items-center gap-2 pb-2">
            <Sparkles className="w-5 h-5 text-[#5AAE62]" />
            <h2 className="text-lg font-bold text-[#111111]">
              Recipes you can cook now{' '}
              <span className="text-xs font-normal text-[#555555]">
                (using pantry items)
              </span>
            </h2>
          </div>

          {isLoading ? (
            <div className="flex flex-col items-center justify-center min-h-[30vh] py-12 bg-white border border-[#D5D8D5] rounded-2xl">
              <Spinner size="lg" />
              <span className="mt-4 text-sm font-semibold text-[#555555]">
                Matching recipes with your pantry...
              </span>
            </div>
          ) : filteredRecipes.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {filteredRecipes.map((item) => {
                const r = item.recipe;
                const totalMinutes = ((r.prepTimeMinutes || 0) + (r.cookTimeMinutes || 0)) || 20;
                return (
                  <PantryRecipeCard
                    key={r.id}
                    id={r.id}
                    title={r.title}
                    time={`${totalMinutes} mins`}
                    isAiGenerated={r.isAiGenerated}
                    imageUrl={r.imageUrl}
                    matchedIngredients={item.matchedIngredients}
                    totalIngredients={item.totalIngredients}
                    matchPercentage={item.matchPercentage}
                  />
                );
              })}
            </div>
          ) : (
            <div className="bg-white border border-[#D5D8D5] rounded-2xl p-6">
              <EmptyState
                title={pantryItems.length === 0 ? 'No pantry ingredients' : 'No recipes match your pantry yet'}
                description={
                  pantryItems.length === 0
                    ? 'Add some ingredients to your pantry inventory on the left to see recipe recommendations.'
                    : searchQuery
                    ? `No matching recipes found for "${searchQuery}".`
                    : 'Add a few more staples (like chicken, rice, garlic) to discover matching dishes.'
                }
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
