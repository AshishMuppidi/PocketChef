const TOKEN_KEY = 'whatsonmyplate_token';

/**
 * Retrieves the stored JWT authentication token from localStorage.
 * @returns {string|null} The token string or null if not found.
 */
export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch (err) {
    console.error('Failed to read auth token from storage:', err);
    return null;
  }
}

/**
 * Stores the JWT authentication token in localStorage.
 * @param {string} token - The access token string.
 */
export function setToken(token) {
  try {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_KEY);
    }
  } catch (err) {
    console.error('Failed to save auth token to storage:', err);
  }
}

/**
 * Removes the stored JWT authentication token from localStorage.
 */
export function removeToken() {
  try {
    localStorage.removeItem(TOKEN_KEY);
  } catch (err) {
    console.error('Failed to remove auth token from storage:', err);
  }
}
