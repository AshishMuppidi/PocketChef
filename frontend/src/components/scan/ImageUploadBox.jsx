import { useState, useRef } from 'react';
import { Camera, X, AlertCircle } from 'lucide-react';

export default function ImageUploadBox({
  onImageSelected,
  selectedImage,
  onRemoveImage,
  disabled = false,
  maxSizeBytes = 10 * 1024 * 1024
}) {
  const [isDragging, setIsDragging] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const fileInputRef = useRef(null);

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];

  const validateAndHandleFile = (file) => {
    setErrorMessage(null);
    if (!file) return;

    if (!allowedTypes.includes(file.type.toLowerCase())) {
      setErrorMessage('Please upload a valid JPEG, PNG, or WebP image.');
      return;
    }

    if (file.size > maxSizeBytes) {
      setErrorMessage('Image size must be 10MB or less.');
      return;
    }

    const previewUrl = URL.createObjectURL(file);
    if (onImageSelected) {
      onImageSelected(file, previewUrl);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    if (disabled) return;
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    if (disabled) return;
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndHandleFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      validateAndHandleFile(e.target.files[0]);
    }
  };

  const handleClick = () => {
    if (disabled) return;
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  return (
    <div className="w-full flex flex-col items-center">
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        accept="image/jpeg,image/png,image/webp"
        disabled={disabled}
        className="hidden"
      />

      {selectedImage ? (
        <div className="relative w-full max-w-[560px] h-[340px] md:h-[370px] rounded-2xl overflow-hidden border border-[#D5D8D5] shadow-xs bg-[#F5F6F5]">
          <img
            src={selectedImage.previewUrl}
            alt="Dish Preview"
            className="w-full h-full object-cover"
          />
          {!disabled && (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                if (onRemoveImage) onRemoveImage();
                if (fileInputRef.current) fileInputRef.current.value = '';
              }}
              className="absolute top-3 right-3 p-2 bg-black/70 hover:bg-black text-white rounded-full transition-colors"
              aria-label="Remove uploaded image"
            >
              <X className="w-5 h-5" />
            </button>
          )}
        </div>
      ) : (
        <div
          onClick={handleClick}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          className={`w-full max-w-[560px] h-[340px] md:h-[370px] rounded-2xl border-2 border-dashed flex flex-col items-center justify-center p-6 text-center transition-all ${
            disabled ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer'
          } ${
            isDragging
              ? 'border-[#5AAE62] bg-[#E8F4E9]/40'
              : 'border-[#D5D8D5] bg-[#F5F6F5]/60 hover:bg-[#F5F6F5] hover:border-[#5AAE62]'
          }`}
        >
          <div className="w-16 h-16 rounded-full bg-white shadow-xs border border-[#D5D8D5] flex items-center justify-center text-[#5AAE62] mb-4 group-hover:scale-105 transition-transform">
            <Camera className="w-8 h-8" />
          </div>

          <p className="text-base md:text-lg font-semibold text-[#111111] max-w-xs leading-snug mb-1">
            Click to upload a photo of food
          </p>
          <p className="text-sm md:text-base text-[#555555]">
            or a prepared dish
          </p>

          <span className="mt-4 text-xs font-medium text-[#888888]">
            JPEG, PNG, WebP up to 10MB
          </span>
        </div>
      )}

      {errorMessage && (
        <div className="mt-3 flex items-center gap-1.5 text-xs text-red-600">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}
    </div>
  );
}
