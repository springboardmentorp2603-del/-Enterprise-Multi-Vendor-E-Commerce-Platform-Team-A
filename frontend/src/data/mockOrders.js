import { PRODUCTS_DATA } from './products';

const makeItem = (product, quantity) => ({ productId: product.id, productName: product.name, image: product.image, quantity, price: product.price });
export const MOCK_ORDERS = [
  { orderId: 'SS-2026-10428', status: 'SHIPPED', total: 749.97, paymentStatus: 'PAID', trackingNumber: 'BLUEDART-882019', estimatedDelivery: '2026-07-19', customerName: 'Aarav Mehta', shippingAddress: { addressLine1: '42 Lake View Road', city: 'Bengaluru', state: 'Karnataka', postalCode: '560001' }, items: [makeItem(PRODUCTS_DATA[0], 1), makeItem(PRODUCTS_DATA[1], 1)] },
  { orderId: 'SS-2026-10402', status: 'DELIVERED', total: 169.98, paymentStatus: 'PAID', trackingNumber: 'DELHIVERY-773401', estimatedDelivery: '2026-07-12', customerName: 'Priya Sharma', shippingAddress: { addressLine1: '18 Palm Grove', city: 'Mumbai', state: 'Maharashtra', postalCode: '400001' }, items: [makeItem(PRODUCTS_DATA[5], 1), makeItem(PRODUCTS_DATA[7], 1)] },
  { orderId: 'SS-2026-10371', status: 'PENDING', total: 79.99, paymentStatus: 'PAID', trackingNumber: '', estimatedDelivery: '2026-07-23', customerName: 'Rohan Kapoor', shippingAddress: { addressLine1: '10 Park Street', city: 'Kolkata', state: 'West Bengal', postalCode: '700016' }, items: [makeItem(PRODUCTS_DATA[8], 1)] },
];

export const MOCK_NOTIFICATIONS = [
  { notificationId: 'notice-1', title: 'Order shipped', message: 'Your order SS-2026-10428 is on its way.', isRead: false },
  { notificationId: 'notice-2', title: 'Order delivered', message: 'Order SS-2026-10402 was delivered successfully.', isRead: false },
  { notificationId: 'notice-3', title: 'Order confirmed', message: 'Order SS-2026-10371 has been confirmed.', isRead: true },
];
