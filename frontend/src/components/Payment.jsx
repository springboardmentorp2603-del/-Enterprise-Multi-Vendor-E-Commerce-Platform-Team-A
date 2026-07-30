import React from 'react';
import { api } from '../api';

export default function Payment() {
  return (
    <div className="p-4 bg-white rounded shadow">
      <h2 className="text-xl font-bold">Payment Page</h2>
      <p>Select your payment method (UPI, Credit Card, etc.)</p>
      <button className="bg-purple-500 text-white px-4 py-2 mt-2 rounded">Pay Now</button>
    </div>
  );
}