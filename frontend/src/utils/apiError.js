/**
 * Extracts a user-friendly error message from an API error response.
 * Sanitizes and guards against exposing server internals, stack traces, or credentials.
 *
 * @param {Error|Object} error - The caught error object (Axios error).
 * @param {string} [fallbackMessage='An unexpected error occurred. Please try again.']
 * @returns {string} The formatted error message.
 */
export function getApiErrorMessage(error, fallbackMessage = 'An unexpected error occurred. Please try again.') {
  if (!error) return fallbackMessage;

  // Check if response contains structured error from backend GlobalExceptionHandler
  if (error.response && error.response.data) {
    const data = error.response.data;

    if (typeof data === 'string' && data.trim()) {
      return data;
    }

    if (data.message && typeof data.message === 'string' && data.message.trim()) {
      return data.message;
    }

    if (data.error && typeof data.error === 'string' && data.error.trim()) {
      return data.error;
    }
  }

  // Network error or timeout
  if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
    return 'The request timed out. Please check your connection and try again.';
  }

  if (error.message === 'Network Error' || !error.response) {
    return 'Unable to reach the server. Please verify the backend is running.';
  }

  return fallbackMessage;
}
