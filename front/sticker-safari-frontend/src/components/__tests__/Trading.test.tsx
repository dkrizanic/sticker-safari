import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Trading from '../Trading';
import apiService from '../../api/apiService';

jest.mock('../../api/apiService');

// Mock useUser hook directly
jest.mock('../../context/UserContext', () => ({
  useUser: () => ({
    currentUser: { id: 1, username: 'Bob', stickers: [] },
    login: jest.fn(),
    logout: jest.fn(),
  }),
}));

describe('Trading Component Tests', () => {
  const renderTrading = () => {
    return render(
      <BrowserRouter>
        <Trading />
      </BrowserRouter>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('New Feature: Trading screen implementation', () => {
    test('should fetch trade offers on mount', async () => {
      const mockOffers = [
        { id: 1, fromUser: 'Alice', sticker: 'Lion', status: 'PENDING' }
      ];
      (apiService.getReceivedTradeOffers as jest.Mock).mockResolvedValue(mockOffers);
      (apiService.getSentTradeOffers as jest.Mock).mockResolvedValue([]);

      renderTrading();

      await waitFor(() => {
        expect(apiService.getReceivedTradeOffers).toHaveBeenCalledWith(1);
      });
    });

    test('should display received trade offers', async () => {
      const mockOffers = [
        { 
          sender: { id: 2, username: 'Alice' },
          receiver: { id: 1, username: 'Bob' },
          offeredSticker: { id: 2, name: 'Tiger', latinName: 'Panthera tigris', description: 'Stripes', habitat: 'Forest', src: '/tiger.jpg' },
          receivedSticker: { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg' },
          message: 'Would like to trade',
          status: 'PENDING'
        }
      ];
      (apiService.getReceivedTradeOffers as jest.Mock).mockResolvedValue(mockOffers);
      (apiService.getSentTradeOffers as jest.Mock).mockResolvedValue([]);

      renderTrading();

      await waitFor(() => {
        expect(screen.getByText(/Alice/i)).toBeInTheDocument();
      });
    });
  });
});
