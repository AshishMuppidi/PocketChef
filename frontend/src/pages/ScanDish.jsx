import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, AlertCircle } from 'lucide-react';
import apiClient from '../api/apiClient';
import { getApiErrorMessage } from '../utils/apiError';
import ImageUploadBox from '../components/scan/ImageUploadBox';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';

export default function ScanDish() {
  const [selectedImage, setSelectedImage] = useState(null);
  const [promptNotes, setPromptNotes] = useState('');
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const navigate = useNavigate();

  // Cleanup object URL on unmount to prevent memory leaks
  useEffect(() => {
    return () => {
      if (selectedImage?.previewUrl) {
        URL.revokeObjectURL(selectedImage.previewUrl);
      }
    };
  }, [selectedImage]);

  const handleImageSelected = (file, previewUrl) => {
    // Revoke previous URL if any
    if (selectedImage?.previewUrl) {
      URL.revokeObjectURL(selectedImage.previewUrl);
    }
    setErrorMessage(null);
    setSelectedImage({ file, previewUrl });
  };

  const handleRemoveImage = () => {
    if (selectedImage?.previewUrl) {
      URL.revokeObjectURL(selectedImage.previewUrl);
    }
    setSelectedImage(null);
    setErrorMessage(null);
  };

  const handleAnalyze = async () => {
    if (!selectedImage?.file) {
      setErrorMessage('Please select a photo of food to analyze.');
      return;
    }

    setErrorMessage(null);
    setIsAnalyzing(true);

    try {
      const formData = new FormData();
      formData.append('image', selectedImage.file);

      if (promptNotes.trim()) {
        formData.append('promptNotes', promptNotes.trim());
      }

      // Send multipart request to backend Image-to-Recipe API
      // Axios and the browser automatically compute the Content-Type boundary
      const response = await apiClient.post('/recipes/analyze-image', formData);

      const createdRecipe = response.data;
      if (createdRecipe && createdRecipe.id) {
        navigate(`/recipe/${createdRecipe.id}`);
      } else {
        throw new Error('Analysis completed but no valid recipe ID was returned.');
      }
    } catch (err) {
      let customFallback = 'Failed to analyze dish image. Please try another photo.';
      if (err.response?.status === 503) {
        customFallback = 'AI service is currently unavailable. Please try again in a moment.';
      } else if (err.response?.status === 401) {
        customFallback = 'Please log in to analyze a dish.';
      } else if (err.response?.status === 413) {
        customFallback = 'Uploaded image exceeds the maximum 10MB size limit.';
      }

      const msg = getApiErrorMessage(err, customFallback);
      setErrorMessage(msg);
    } finally {
      setIsAnalyzing(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-[calc(100vh-220px)] py-4">
      {/* Centered Large Card 640-680px */}
      <div className="w-full max-w-[660px] bg-white border border-[#D5D8D5] rounded-3xl p-6 sm:p-10 shadow-xs flex flex-col items-center">
        {/* Large Bold Heading */}
        <h1 className="text-2xl sm:text-3xl font-extrabold text-[#111111] mb-6 text-center tracking-tight">
          Scan a Dish
        </h1>

        {/* Error Alert Banner */}
        {errorMessage && (
          <div className="w-full max-w-[560px] mb-6 p-4 bg-red-50 border border-red-200 rounded-2xl flex items-start gap-3 text-sm text-red-700">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <div className="flex-1">
              <span className="font-semibold block mb-0.5">Analysis Notice</span>
              <span>{errorMessage}</span>
            </div>
          </div>
        )}

        {/* Upload Dropzone Area */}
        <div className="w-full mb-6">
          <ImageUploadBox
            selectedImage={selectedImage}
            onImageSelected={handleImageSelected}
            onRemoveImage={handleRemoveImage}
            disabled={isAnalyzing}
          />
        </div>

        {/* Optional Prompt Notes Textarea */}
        <div className="w-full max-w-[560px] mb-8">
          <div className="flex items-center justify-between mb-1.5">
            <label
              htmlFor="prompt-notes"
              className="text-xs font-semibold text-[#555555]"
            >
              Additional notes (optional)
            </label>
            <span className="text-[11px] text-[#888888]">
              {promptNotes.length} / 1000
            </span>
          </div>
          <textarea
            id="prompt-notes"
            rows={2}
            maxLength={1000}
            disabled={isAnalyzing}
            value={promptNotes}
            onChange={(e) => setPromptNotes(e.target.value)}
            placeholder="Example: Make it less spicy, vegetarian, or suitable for 4 people."
            className="w-full bg-[#F5F6F5] border border-[#D5D8D5] rounded-xl p-3 text-xs sm:text-sm text-[#111111] placeholder-[#888888] focus:border-[#5AAE62] focus:ring-2 focus:ring-[#5AAE62]/20 focus:outline-none transition-colors resize-none disabled:opacity-60"
          />
        </div>

        {/* Analyze Dish Button */}
        <div className="w-full max-w-[560px]">
          <Button
            size="lg"
            disabled={!selectedImage || isAnalyzing}
            onClick={handleAnalyze}
            className="w-full py-3.5 text-base font-bold rounded-xl shadow-xs"
          >
            {isAnalyzing ? (
              <span className="flex items-center justify-center gap-2.5">
                <Spinner size="sm" className="border-white" />
                Analyzing dish with Gemini AI...
              </span>
            ) : (
              <span className="flex items-center justify-center gap-2">
                <Sparkles className="w-5 h-5" />
                Analyze Dish
              </span>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
}
