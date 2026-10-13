import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Star, UploadCloud, CheckCircle, AlertCircle } from 'lucide-react';
import apiClient from '../api/apiClient';
import { getApiErrorMessage } from '../utils/apiError';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Spinner from '../components/common/Spinner';

export default function CreateCommunityPost() {
  const navigate = useNavigate();
  const [recipes, setRecipes] = useState([]);
  const [selectedRecipeId, setSelectedRecipeId] = useState('');
  const [selectedImageFile, setSelectedImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [caption, setCaption] = useState('');
  const [rating, setRating] = useState(5);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  // Fetch real recipes to populate recipe select
  useEffect(() => {
    async function loadRecipes() {
      try {
        const response = await apiClient.get('/recipes', { params: { size: 50 } });
        const list = response.data?.content || [];
        setRecipes(list);
        if (list.length > 0) {
          setSelectedRecipeId(list[0].id.toString());
        }
      } catch (err) {
        console.error('Failed to load recipes for remake:', err);
      }
    }
    loadRecipes();
  }, []);

  // Cleanup object URL on unmount
  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  const handleImageChange = (e) => {
    setErrorMessage(null);
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      const allowed = ['image/jpeg', 'image/png', 'image/webp'];

      if (!allowed.includes(file.type.toLowerCase())) {
        setErrorMessage('Please upload a JPEG, PNG, or WebP photo.');
        return;
      }

      if (file.size > 10 * 1024 * 1024) {
        setErrorMessage('Photo size must be 10MB or less.');
        return;
      }

      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }

      setSelectedImageFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedImageFile) {
      setErrorMessage('Please upload a photo of your prepared dish.');
      return;
    }
    if (!caption.trim() || isSubmitting) return;

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const formData = new FormData();
      formData.append('image', selectedImageFile);
      if (selectedRecipeId) {
        formData.append('recipeId', selectedRecipeId);
      }
      formData.append('caption', caption.trim());
      formData.append('rating', rating.toString());

      await apiClient.post('/community/posts', formData);

      navigate('/community');
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Failed to publish remake post. Please try again.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6 pb-12">
      <Link
        to="/community"
        className="inline-flex items-center gap-1.5 text-sm font-semibold text-[#555555] hover:text-[#5AAE62] transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Back to Community Feed</span>
      </Link>

      <Card className="p-6 sm:p-8">
        <h1 className="text-2xl font-extrabold text-[#111111] mb-2 tracking-tight">
          Post a Recipe Remake
        </h1>
        <p className="text-sm text-[#555555] mb-6">
          Show the community how your dish turned out and rate the recipe.
        </p>

        {errorMessage && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-2xl flex items-start gap-2.5 text-sm text-red-700">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Select Recipe */}
          <div>
            <label htmlFor="recipe-select" className="block text-sm font-semibold text-[#111111] mb-1.5">
              Which recipe did you cook?
            </label>
            <select
              id="recipe-select"
              value={selectedRecipeId}
              onChange={(e) => setSelectedRecipeId(e.target.value)}
              disabled={isSubmitting}
              className="w-full bg-white border border-[#D5D8D5] rounded-xl px-3.5 py-2.5 text-sm font-medium text-[#111111] focus:border-[#5AAE62] focus:ring-2 focus:ring-[#5AAE62]/20 focus:outline-none disabled:opacity-60"
            >
              <option value="">-- General Culinary Creation (No recipe) --</option>
              {recipes.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.title}
                </option>
              ))}
            </select>
          </div>

          {/* Photo Upload with Preview */}
          <div>
            <label className="block text-sm font-semibold text-[#111111] mb-1.5">
              Upload Food Photo
            </label>
            <div className="border-2 border-dashed border-[#D5D8D5] rounded-2xl p-4 flex flex-col items-center justify-center bg-[#F5F6F5]/50">
              {previewUrl ? (
                <div className="relative w-full aspect-16/10 rounded-xl overflow-hidden mb-3">
                  <img src={previewUrl} alt="Remake Preview" className="w-full h-full object-cover" />
                </div>
              ) : (
                <div className="text-center py-6">
                  <UploadCloud className="w-10 h-10 text-[#5AAE62] mx-auto mb-2" />
                  <p className="text-sm font-medium text-[#111111]">Click below to choose a photo</p>
                  <span className="text-xs text-[#888888]">JPEG, PNG, WebP up to 10MB</span>
                </div>
              )}
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                disabled={isSubmitting}
                onChange={handleImageChange}
                className="text-xs text-[#555555] file:mr-3 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-[#E8F4E9] file:text-[#3F8F49] hover:file:bg-[#5AAE62] hover:file:text-white file:cursor-pointer cursor-pointer disabled:opacity-60"
              />
            </div>
          </div>

          {/* Rating */}
          <div>
            <label className="block text-sm font-semibold text-[#111111] mb-2">
              Your Rating (1 to 5 Stars)
            </label>
            <div className="flex items-center gap-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  type="button"
                  key={star}
                  disabled={isSubmitting}
                  onClick={() => setRating(star)}
                  className="p-1 text-amber-400 hover:scale-110 transition-transform disabled:hover:scale-100"
                >
                  <Star
                    size={28}
                    className={star <= rating ? 'fill-amber-400 text-amber-400' : 'text-gray-300'}
                  />
                </button>
              ))}
              <span className="text-sm font-bold text-[#111111] ml-2">
                {rating}.0 / 5.0
              </span>
            </div>
          </div>

          {/* Caption */}
          <div>
            <label htmlFor="caption" className="block text-sm font-semibold text-[#111111] mb-1.5">
              Caption & Tasting Notes
            </label>
            <textarea
              id="caption"
              rows={3}
              required
              maxLength={2000}
              disabled={isSubmitting}
              placeholder="Tell others how it tasted, any spice adjustments or tweaks you made..."
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
              className="w-full bg-white border border-[#D5D8D5] rounded-xl p-3.5 text-sm text-[#111111] placeholder-[#888888] focus:border-[#5AAE62] focus:ring-2 focus:ring-[#5AAE62]/20 focus:outline-none disabled:opacity-60"
            />
          </div>

          {/* Submit Button */}
          <Button
            type="submit"
            size="lg"
            disabled={isSubmitting || !selectedImageFile || !caption.trim()}
            className="w-full py-3 font-bold"
          >
            {isSubmitting ? (
              <span className="flex items-center justify-center gap-2">
                <Spinner size="sm" className="border-white" />
                Uploading & Publishing Remake...
              </span>
            ) : (
              'Publish Remake Post'
            )}
          </Button>
        </form>
      </Card>
    </div>
  );
}
