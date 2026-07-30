export default function AvailabilityBadge({ stockQty }) {
  let label = "In Stock";

  if (stockQty <= 0) label = "Out of Stock";
  else if (stockQty <= 10) label = "Low Stock";

  return (
    <span
      style={{
        background: "transparent",
        color: "#444",
        border: "1px solid #ccc",
        padding: "4px 10px",
        borderRadius: "20px",
        fontSize: "12px",
        fontWeight: 600
      }}
    >
      {label}
    </span>
  );
}