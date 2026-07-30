import { useMemo } from 'react';

/**
 * Hook to filter products based on price range, selected category, and search keyword.
 * @param {Array} products - Original list of products.
 * @param {Object} options - Filtering options.
 * @param {string|number} options.priceMin - Minimum price (inclusive).
 * @param {string|number} options.priceMax - Maximum price (inclusive).
 * @param {string|number|null} options.selectedCategory - Category ID to filter by.
 * @param {string} options.searchKeyword - Keyword for name/description search.
 * @returns {Array} Filtered products.
 */
export default function useFilteredProducts(products = [], { priceMin, priceMax, selectedCategory, searchKeyword } = {}) {
  const parsedMin = Number.parseFloat(priceMin);
  const parsedMax = Number.parseFloat(priceMax);
  const min = Number.isFinite(parsedMin) ? parsedMin : null;
  const max = Number.isFinite(parsedMax) ? parsedMax : null;
  const keyword = searchKeyword ? searchKeyword.trim().toLowerCase() : '';

  return useMemo(() => {
    return products.filter(p => {
      const price = Number.parseFloat(p.price);
      const productPrice = Number.isFinite(price) ? price : 0;
      if (min !== null && productPrice < min) return false;
      if (max !== null && productPrice > max) return false;
      if (
        selectedCategory &&
        selectedCategory !== 'all' &&
        String(p.category || '').toLowerCase() !== String(selectedCategory).toLowerCase()
      ) return false;
      if (keyword) {
        const name = (p.productName || p.name || '').toLowerCase();
        const desc = (p.description || '').toLowerCase();
        if (!name.includes(keyword) && !desc.includes(keyword)) return false;
      }
      return true;
    });
  }, [products, min, max, selectedCategory, keyword]);
}
