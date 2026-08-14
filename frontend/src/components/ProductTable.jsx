import AvailabilityBadge from "./AvailabilityBadge";
import "./ProductTable.css";

export default function ProductTable({ products, onSelectStock, onSelectDiscount }) {
  if (!products.length) {
    return <p className="empty-state">No products yet. Add one to get started.</p>;
  }

  return (
    <table className="inventory-table">
      <thead>
        <tr>
          <th>Product</th>
          <th>SKU</th>
          <th>Stock Qty</th>
          <th>Price</th>
          <th>Discount</th>
          <th>Final Price</th>
          <th>Status</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {products.map((p) => {
          const discount = p.discountPercentage ?? p.discountPercent ?? 0;
          const finalPrice = p.price - (p.price * discount) / 100;
          return (
            <tr key={p.id}>
              <td>{p.name}</td>
              <td className="mono">{p.sku}</td>
              <td>{p.stockQty}</td>
              <td>₹{p.price.toFixed(2)}</td>
              <td>{discount}%</td>
              <td>₹{finalPrice.toFixed(2)}</td>
              <td>
                <AvailabilityBadge stockQty={p.stockQty} />
              </td>
              <td className="actions">
                <button className="btn-link" onClick={() => onSelectStock(p)}>
                  Update Stock
                </button>
                <button className="btn-link" onClick={() => onSelectDiscount(p)}>
                  Set Discount
                </button>
              </td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}
