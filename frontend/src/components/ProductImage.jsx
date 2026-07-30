import React from "react";

export default function ProductImage({ src, alt, className, ...props }) {
  return (
    <img
      src={src}
      alt={alt}
      className={className}
      {...props}
      loading="lazy"
      onError={(e) => {
        e.target.src = "https://via.placeholder.com/300x300?text=No+Image";
      }}
    />
  );
}
