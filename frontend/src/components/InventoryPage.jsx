import { useEffect, useState } from "react";
import ProductTable from "./ProductTable";
import "./InventoryPage.css";

// Mock data — replace fetchProducts() below with your real API call
// once Backend 1 (GET /api/inventory) is ready.
const MOCK_PRODUCTS = [
  { id: 1, name: "Wireless Mouse", sku: "WM-1001", stockQty: 45, price: 799, discountPercent: 10 },
  { id: 2, name: "Mechanical Keyboard", sku: "MK-2003", stockQty: 8, price: 2999, discountPercent: 0 },
  { id: 3, name: "USB-C Hub", sku: "UH-3050", stockQty: 0, price: 1299, discountPercent: 15 },
];

async function fetchProducts() {
  // --- Swap this block for the real call when Backend 1 is ready ---
  // const res = await fetch("/api/inventory", {
  //   headers: { Authorization: `Bearer ${token}` },
  // });
  // if (!res.ok) throw new Error("Failed to load inventory");
  // return res.json();
  return new Promise((resolve) => setTimeout(() => resolve(MOCK_PRODUCTS), 300));
}

export default function InventoryPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProducts()
      .then(setProducts)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleSelectStock = (product) => {
    // Wired up in the next step: Stock Update modal
    console.log("Update stock for", product);
  };

  const handleSelectDiscount = (product) => {
    // Wired up in the next step: Discount modal
    console.log("Set discount for", product);
  };

  if (loading) return <p className="status-msg">Loading inventory...</p>;
  if (error) return <p className="status-msg error">Couldn't load inventory: {error}</p>;

  return (
    <div className="inventory-page">
      <div className="inventory-header">
        <h1>Inventory</h1>
        <span className="count">{products.length} products</span>
      </div>
      <ProductTable
        products={products}
        onSelectStock={handleSelectStock}
        onSelectDiscount={handleSelectDiscount}
      />
    </div>
  );
}
