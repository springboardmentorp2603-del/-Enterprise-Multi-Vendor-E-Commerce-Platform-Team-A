import React from 'react';
import { Eye, RotateCcw, XCircle, Calendar, MapPin } from 'lucide-react';
import ProductImage from './ProductImage';
import StatusBadge from './StatusBadge';
import InvoiceButton from './InvoiceButton';

const vendorStatuses = [
  'PENDING',
  'CONFIRMED',
  'PACKED',
  'SHIPPED',
  'OUT_FOR_DELIVERY',
  'DELIVERED',
  'CANCELLED',
  'RETURNED'
];

export default function OrderCard({
  order,
  isVendor,
  onStatusChange,
  onOpenDetails,
  onCancel,
  onReturn,
  onReorder
}) {
  const orderId = order.orderId || order.id;

  const normalizedStatus = String(
    order.status || 'PENDING'
  ).toUpperCase().replace(/ /g, '_');

  const items = order.items || [];

  const canCancel = ['PENDING', 'CONFIRMED'].includes(normalizedStatus);

  const totalVal = Number(
    order.totalAmount ||
    order.total ||
    order.amount ||
    0
  );

  // -----------------------------
  // ORDER DATE & TIME
  // -----------------------------
  const rawDate =
    order.createdAt ||
    order.orderDate ||
    order.date ||
    order.createdDate;

  const formattedDate = rawDate
    ? new Date(rawDate).toLocaleString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
      })
    : 'Date not available';

  // -----------------------------
  // SHIPPING ADDRESS
  // -----------------------------
  const shippingAddress =
    order.shippingAddress ||
    order.address ||
    order.deliveryAddress ||
    order.customerAddress;

  const getAddressText = (address) => {
    if (!address) {
      return 'Address not available';
    }

    // If backend already sends address as a string
    if (typeof address === 'string') {
      return address;
    }

    // If backend sends address as an object
    return [
      address.addressLine1,
      address.addressLine2,
      address.street,
      address.city,
      address.state,
      address.postalCode || address.zipCode || address.pincode,
      address.country
    ]
      .filter(Boolean)
      .join(', ') || 'Address not available';
  };

  const addressText = getAddressText(shippingAddress);

  return (
    <article
      className="order-card glass-card"
      style={{
        padding: '1rem',
        borderRadius: '8px',
        background: 'var(--bg-card)',
        boxShadow: 'var(--shadow-md)'
      }}
    >

      {/* HEADER */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          gap: '10px',
          alignItems: 'start'
        }}
      >
        <div>
          <strong>Order #{orderId}</strong>

          <div
            style={{
              fontSize: '0.8rem',
              color: 'var(--text-muted)',
              marginTop: 4
            }}
          >
            {order.customerName ||
              order.customer?.name ||
              order.user?.name ||
              (isVendor ? 'Customer details' : '')}
          </div>
        </div>

        <StatusBadge status={normalizedStatus} />
      </div>

      {/* DATE & TIME */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          marginTop: '10px',
          fontSize: '0.8rem',
          color: 'var(--text-secondary)'
        }}
      >
        <Calendar size={14} />
        <span>{formattedDate}</span>
      </div>

      {/* SHIPPING ADDRESS */}
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: '6px',
          marginTop: '8px',
          fontSize: '0.8rem',
          color: 'var(--text-secondary)'
        }}
      >
        <MapPin size={14} style={{ flexShrink: 0, marginTop: 2 }} />

        <span>
          {addressText}
        </span>
      </div>

      {/* PRODUCTS */}
      <div
        style={{
          margin: '12px 0',
          display: 'grid',
          gap: '8px'
        }}
      >
        {items.slice(0, 2).map((item, index) => {

          const imageUrl =
            item.imageUrl ||
            item.image ||
            item.productImage ||
            item.product?.imageUrl ||
            item.product?.image ||
            item.product?.imageUrlPath;

          const productName =
            item.productName ||
            item.name ||
            item.product?.productName ||
            item.product?.name ||
            'Product';

          const itemPrice = Number(
            item.price ||
            item.unitPrice ||
            item.product?.price ||
            0
          );

          return (
            <div
              key={item.id || item.productId || index}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >

              {/* PRODUCT IMAGE */}
              <ProductImage
                src={imageUrl}
                alt={productName}
                style={{
                  width: 48,
                  height: 48,
                  borderRadius: 6,
                  objectFit: 'cover',
                  flexShrink: 0
                }}
              />

              {/* PRODUCT NAME */}
              <span
                style={{
                  flex: 1,
                  fontSize: '0.85rem'
                }}
              >
                {productName} × {item.quantity || 1}
              </span>

              {/* PRODUCT PRICE */}
              <span
                style={{
                  fontSize: '0.82rem'
                }}
              >
                ₹{itemPrice.toFixed(2)}
              </span>

            </div>
          );
        })}
      </div>

      {/* PAYMENT + TOTAL */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          borderTop: '1px solid var(--border-color)',
          paddingTop: '10px'
        }}
      >
        <span
          style={{
            fontSize: '0.82rem',
            color: 'var(--text-secondary)'
          }}
        >
          Payment: {order.paymentStatus || 'COMPLETED'}
        </span>

        <strong>
          ₹{totalVal.toFixed(2)}
        </strong>
      </div>

      {/* ACTIONS */}
      <div
        style={{
          display: 'flex',
          gap: '8px',
          flexWrap: 'wrap',
          marginTop: '12px'
        }}
      >

        <button
          className="btn btn-secondary"
          onClick={() => onOpenDetails(order)}
          style={{
            padding: '0.35rem 0.65rem',
            fontSize: '0.8rem'
          }}
        >
          <Eye size={14} />
          Details
        </button>

        {!isVendor && (
          <>
            <InvoiceButton order={order} />

            <button
              className="btn btn-secondary"
              onClick={() => onReorder(order)}
              style={{
                padding: '0.35rem 0.65rem',
                fontSize: '0.8rem'
              }}
            >
              <RotateCcw size={14} />
              Reorder
            </button>

            {canCancel && (
              <button
                className="btn btn-secondary"
                onClick={() => onCancel(order)}
                style={{
                  padding: '0.35rem 0.65rem',
                  fontSize: '0.8rem',
                  color: '#ef4444'
                }}
              >
                <XCircle size={14} />
                Cancel
              </button>
            )}

            {normalizedStatus === 'DELIVERED' && (
              <button
                className="btn btn-secondary"
                onClick={() => onReturn(order)}
                style={{
                  padding: '0.35rem 0.65rem',
                  fontSize: '0.8rem'
                }}
              >
                Return
              </button>
            )}
          </>
        )}

        {isVendor && (
          <select
            value={
              vendorStatuses.includes(normalizedStatus)
                ? normalizedStatus
                : 'PENDING'
            }
            onChange={(event) =>
              onStatusChange(orderId, event.target.value)
            }
            style={{
              marginLeft: 'auto',
              background: 'var(--input-bg)',
              color: 'var(--text-primary)',
              border: '1px solid var(--border-color)',
              borderRadius: '6px',
              padding: '0.35rem',
              cursor: 'pointer'
            }}
          >
            {vendorStatuses.map((status) => (
              <option key={status} value={status}>
                {status.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        )}

      </div>

    </article>
  );
}