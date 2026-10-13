import { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search, AlertCircle, RefreshCw, X } from 'lucide-react';
import apiClient from '../api/apiClient';
import { getApiErrorMessage } from '../utils/apiError';
import RecipeCard from '../components/recipe/RecipeCard';
import Input from '../components/common/Input';
import Spinner from '../components/common/Spinner';
import EmptyState from '../components/common/EmptyState';
import Button from '../components/common/Button';

export default function SearchRecipes() {
  const [searchParams, setSearchParams] = useSearchParams();

  // Read initial params from URL
  const queryParam = searchParams.get('q') || '';
  const sourceParam = searchParams.get('source') || '';
  const spiceParam = searchParams.get('spiceLevel') || '';
  const maxPrepParam = searchParams.get('maxPrepTime') || '';
  const pageParam = parseInt(searchParams.get('page') || '0', 10);

  const [searchTerm, setSearchTerm] = useState(queryParam);
  const [sourceFilter, setSourceFilter] = useState(sourceParam);
  const [spiceFilter, setSpiceFilter] = useState(spiceParam);
  const [maxPrepFilter, setMaxPrepFilter] = useState(maxPrepParam);
  const [currentPage, setCurrentPage] = useState(isNaN(pageParam) ? 0 : pageParam);

  const [recipes, setRecipes] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState(null);

  // Sync state if URL search parameters change externally
  useEffect(() => {
    setSearchTerm(searchParams.get('q') || '');
    setSourceFilter(searchParams.get('source') || '');
    setSpiceFilter(searchParams.get('spiceLevel') || '');
    setMaxPrepFilter(searchParams.get('maxPrepTime') || '');
    const p = parseInt(searchParams.get('page') || '0', 10);
    setCurrentPage(isNaN(p) ? 0 : p);
  }, [searchParams]);

  // Update URL helper
  const updateUrlParams = (newParams) => {
    const params = new URLSearchParams(searchParams);

    Object.entries(newParams).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '' && value !== 'ALL') {
        params.set(key, value.toString());
      } else {
        params.delete(key);
      }
    });

    setSearchParams(params);
  };

  // Debounced search text handler
  useEffect(() => {
    const handler = setTimeout(() => {
      if (searchTerm !== queryParam) {
        updateUrlParams({ q: searchTerm, page: 0 });
      }
    }, 350);

    return () => clearTimeout(handler);
  }, [searchTerm, queryParam]);

  // Fetch recipes from backend API
  const fetchRecipes = async (isMountedRef) => {
    setIsLoading(true);
    setErrorMessage(null);

    const params = {
      page: currentPage,
      size: 12,
      sort: 'createdAt,desc'
    };

    if (queryParam.trim()) params.q = queryParam.trim();
    if (sourceParam && sourceParam !== 'ALL') params.source = sourceParam;
    if (spiceParam && spiceParam !== 'ALL') params.spiceLevel = spiceParam;
    if (maxPrepParam) params.maxPrepTime = parseInt(maxPrepParam, 10);

    try {
      const response = await apiClient.get('/recipes', { params });
      if (isMountedRef.current) {
        const data = response.data;
        setRecipes(data.content || []);
        setTotalElements(data.totalElements || 0);
        setTotalPages(data.totalPages || 0);
      }
    } catch (err) {
      if (isMountedRef.current) {
        setErrorMessage(getApiErrorMessage(err, 'Unable to load recipes. Please try again.'));
      }
    } finally {
      if (isMountedRef.current) {
        setIsLoading(false);
      }
    }
  };

  const isMountedRef = useRef(true);
  useEffect(() => {
    isMountedRef.current = true;
    fetchRecipes(isMountedRef);

    return () => {
      isMountedRef.current = false;
    };
  }, [queryParam, sourceParam, spiceParam, maxPrepParam, currentPage]);

  const handleSourceChange = (newSource) => {
    const value = newSource === 'ALL' ? '' : newSource;
    setSourceFilter(value);
    updateUrlParams({ source: value, page: 0 });
  };

  const handleSpiceChange = (newSpice) => {
    const value = newSpice === 'ALL' ? '' : newSpice;
    setSpiceFilter(value);
    updateUrlParams({ spiceLevel: value, page: 0 });
  };

  const handleMaxPrepChange = (newTime) => {
    const value = newTime === 'ALL' ? '' : newTime;
    setMaxPrepFilter(value);
    updateUrlParams({ maxPrepTime: value, page: 0 });
  };

  const handlePageChange = (newPage) => {
    if (newPage < 0 || newPage >= totalPages) return;
    setCurrentPage(newPage);
    updateUrlParams({ page: newPage });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setSourceFilter('');
    setSpiceFilter('');
    setMaxPrepFilter('');
    setCurrentPage(0);
    setSearchParams({});
  };

  const hasActiveFilters = !!(queryParam || sourceParam || spiceParam || maxPrepParam);

  return (
    <div className="space-y-8 pb-12">
      {/* Header & Search Bar */}
      <div className="max-w-3xl mx-auto text-center space-y-4">
        <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-[#111111] tracking-tight">
          Search Recipes
        </h1>
        <p className="text-sm text-[#555555]">
          Explore delicious AI-crafted recipes and community favorites.
        </p>

        {/* Search Input Box */}
        <div className="relative">
          <Input
            id="recipe-search"
            icon={Search}
            placeholder="Search by dish name, ingredient, or cuisine..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="py-3 text-base rounded-xl shadow-xs pr-10"
          />
          {searchTerm && (
            <button
              type="button"
              onClick={() => {
                setSearchTerm('');
                updateUrlParams({ q: '', page: 0 });
              }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-[#888888] hover:text-[#111111] p-1"
              aria-label="Clear search"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Filter Controls Bar */}
        <div className="space-y-3 pt-2">
          {/* Source Filter */}
          <div className="flex flex-wrap items-center justify-center gap-2">
            <span className="text-xs font-semibold text-[#888888] mr-1">Source:</span>
            {[
              { id: 'ALL', label: 'All' },
              { id: 'AI_GENERATED', label: 'AI Generated' },
              { id: 'USER_CREATED', label: 'Community' }
            ].map((pill) => {
              const isActive = (!sourceFilter && pill.id === 'ALL') || sourceFilter === pill.id;
              return (
                <button
                  key={pill.id}
                  type="button"
                  onClick={() => handleSourceChange(pill.id)}
                  className={`px-3 py-1 rounded-full text-xs font-bold transition-all ${
                    isActive
                      ? 'bg-[#5AAE62] text-white shadow-xs'
                      : 'bg-[#F5F6F5] text-[#555555] hover:bg-[#E8F4E9] hover:text-[#3F8F49] border border-[#D5D8D5]'
                  }`}
                >
                  {pill.label}
                </button>
              );
            })}
          </div>

          {/* Spice & Prep Time Filters */}
          <div className="flex flex-wrap items-center justify-center gap-4 text-xs">
            {/* Spice Level */}
            <div className="flex items-center gap-1.5">
              <span className="font-semibold text-[#888888]">Spice:</span>
              {[
                { id: 'ALL', label: 'All' },
                { id: 'MILD', label: 'Mild' },
                { id: 'MEDIUM', label: 'Medium' },
                { id: 'SPICY', label: 'Spicy' }
              ].map((pill) => {
                const isActive = (!spiceFilter && pill.id === 'ALL') || spiceFilter === pill.id;
                return (
                  <button
                    key={pill.id}
                    type="button"
                    onClick={() => handleSpiceChange(pill.id)}
                    className={`px-2.5 py-0.5 rounded-md font-medium transition-colors ${
                      isActive
                        ? 'bg-[#3F8F49] text-white font-bold'
                        : 'bg-[#F5F6F5] text-[#555555] hover:bg-[#E8F4E9]'
                    }`}
                  >
                    {pill.label}
                  </button>
                );
              })}
            </div>

            {/* Prep Time */}
            <div className="flex items-center gap-1.5">
              <span className="font-semibold text-[#888888]">Prep Time:</span>
              {[
                { id: 'ALL', label: 'Any' },
                { id: '15', label: '≤ 15m' },
                { id: '25', label: '≤ 25m' },
                { id: '45', label: '≤ 45m' }
              ].map((pill) => {
                const isActive = (!maxPrepFilter && pill.id === 'ALL') || maxPrepFilter === pill.id;
                return (
                  <button
                    key={pill.id}
                    type="button"
                    onClick={() => handleMaxPrepChange(pill.id)}
                    className={`px-2.5 py-0.5 rounded-md font-medium transition-colors ${
                      isActive
                        ? 'bg-[#3F8F49] text-white font-bold'
                        : 'bg-[#F5F6F5] text-[#555555] hover:bg-[#E8F4E9]'
                    }`}
                  >
                    {pill.label}
                  </button>
                );
              })}
            </div>

            {hasActiveFilters && (
              <button
                type="button"
                onClick={handleClearFilters}
                className="text-xs font-bold text-red-600 hover:text-red-700 underline ml-2"
              >
                Reset All
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Error Alert */}
      {errorMessage && (
        <div className="max-w-2xl mx-auto p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center justify-between gap-3 text-sm text-red-700">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span>{errorMessage}</span>
          </div>
          <Button
            size="sm"
            variant="secondary"
            icon={RefreshCw}
            onClick={() => fetchRecipes(isMountedRef)}
          >
            Retry
          </Button>
        </div>
      )}

      {/* Main Content Area */}
      {isLoading ? (
        <div className="flex flex-col items-center justify-center min-h-[30vh] py-12">
          <Spinner size="lg" />
          <span className="mt-4 text-sm font-semibold text-[#555555]">
            Searching recipes...
          </span>
        </div>
      ) : recipes.length > 0 ? (
        <div className="space-y-8">
          <div className="flex items-center justify-between text-xs font-semibold text-[#888888] px-1">
            <span>
              Showing {recipes.length} of {totalElements} recipes
            </span>
            <span>
              Page {currentPage + 1} of {totalPages || 1}
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {recipes.map((recipe) => (
              <RecipeCard
                key={recipe.id}
                id={recipe.id}
                title={recipe.title}
                description={recipe.description}
                imageUrl={recipe.imageUrl}
                prepTimeMinutes={recipe.prepTimeMinutes}
                cookTimeMinutes={recipe.cookTimeMinutes}
                isAiGenerated={recipe.isAiGenerated}
                spiceLevel={recipe.spiceLevel}
              />
            ))}
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 pt-6 border-t border-[#D5D8D5]">
              <Button
                variant="secondary"
                size="sm"
                disabled={currentPage === 0}
                onClick={() => handlePageChange(currentPage - 1)}
              >
                Previous
              </Button>

              <span className="text-xs font-bold text-[#555555] px-2">
                Page {currentPage + 1} of {totalPages}
              </span>

              <Button
                variant="secondary"
                size="sm"
                disabled={currentPage >= totalPages - 1}
                onClick={() => handlePageChange(currentPage + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </div>
      ) : (
        <div className="max-w-2xl mx-auto py-8">
          <EmptyState
            title="No recipes found"
            description={
              queryParam
                ? `No recipes matched "${queryParam}". Try a different dish name or remove active filters.`
                : 'No recipes match the selected filters.'
            }
            actionLabel={hasActiveFilters ? 'Clear Filters' : undefined}
            onAction={hasActiveFilters ? handleClearFilters : undefined}
          />
        </div>
      )}
    </div>
  );
}
