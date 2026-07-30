import React from 'react';
import { api } from '../api';

export default function Cart() {
  return (
    <div className="p-4 bg-white rounded shadow">
      <h2 className="text-xl font-bold">Shopping Cart</h2>
      <p>Cart items will appear here.</p>
      <button className="bg-blue-500 text-white px-4 py-2 mt-2 rounded">Checkout</button>
    </div>
  );
}