/**
 * Compresses and resizes an image file using the browser Canvas API.
 * Supports aspect-ratio preservation and conversion to JPEG/WebP.
 *
 * @param {File} file - The original uploaded image file.
 * @param {Object} options - Compression options.
 * @param {number} [options.maxWidth=1920] - Maximum width in pixels.
 * @param {number} [options.maxHeight=1080] - Maximum height in pixels.
 * @param {number} [options.quality=0.85] - Compression quality from 0.0 to 1.0.
 * @param {string} [options.outputType='image/jpeg'] - Output MIME type.
 * @returns {Promise<Blob>} - Compressed image Blob.
 */
export async function compressImage(file, options = {}) {
  const {
    maxWidth = 1920,
    maxHeight = 1080,
    quality = 0.85,
    outputType = 'image/jpeg'
  } = options;

  if (!file || !file.type.startsWith('image/')) {
    throw new Error('Provided file is not a valid image.');
  }

  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (event) => {
      const img = new Image();

      img.onload = () => {
        let width = img.width;
        let height = img.height;

        // Calculate new dimensions preserving aspect ratio
        if (width > maxWidth) {
          height = Math.round((height * maxWidth) / width);
          width = maxWidth;
        }

        if (height > maxHeight) {
          width = Math.round((width * maxHeight) / height);
          height = maxHeight;
        }

        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;

        const ctx = canvas.getContext('2d');
        if (!ctx) {
          reject(new Error('Failed to get 2D canvas context.'));
          return;
        }

        // Draw and compress image
        ctx.drawImage(img, 0, 0, width, height);

        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob);
            } else {
              reject(new Error('Canvas image compression failed.'));
            }
          },
          outputType,
          quality
        );
      };

      img.onerror = () => {
        reject(new Error('Failed to load image for compression.'));
      };

      img.src = event.target.result;
    };

    reader.onerror = () => {
      reject(new Error('Failed to read image file.'));
    };

    reader.readAsDataURL(file);
  });
}
