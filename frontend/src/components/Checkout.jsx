import React from 'react';
import { api } from '../api';

export default function Checkout() {
  return (
    <div className="p-4 bg-white rounded shadow">
      <h2 className="text-xl font-bold">Checkout</h2>
      <p>Enter delivery address and details.</p>
      <button className="bg-green-500 text-white px-4 py-2 mt-2 rounded">Proceed to Payment</button>
    </div>
  );
}