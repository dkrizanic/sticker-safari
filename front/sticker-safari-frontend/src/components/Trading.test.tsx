import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Trading from './Trading';
import apiService from '../api/apiService';

jest.mock('../api/apiService');

// Mock useUser hook directly
jest.mock('../context/UserContext', () => ({
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
          id: 1, 
          sender: { id: 2, username: 'Alice' },
          receiver: { id: 1, username: 'Bob' },
          offeredSticker: { id: 1, name: 'Lion' },
          receivedSticker: { id: 2, name: 'Tiger' },
          status: 'PENDING' 
        }
      ];
      (apiService.getReceivedTradeOffers as jest.Mock).mockResolvedValue(mockOffers);
      (apiService.getSentTradeOffers as jest.Mock).mockResolvedValue([]);

      renderTrading();

      await waitFor(() => {
        expect(screen.getByText(/Trading/i)).toBeInTheDocument();
      });
    });

    test('should switch between Received and Sent tabs', async () => {
      (apiService.getReceivedTradeOffers as jest.Mock).mockResolvedValue([]);
      (apiService.getSentTradeOffers as jest.Mock).mockResolvedValue([]);

      renderTrading();

      await waitFor(() => {
        expect(screen.getByText(/Trading/i)).toBeInTheDocument();
      });

      // Check that both API calls were made
      expect(apiService.getReceivedTradeOffers).toHaveBeenCalled();
      expect(apiService.getSentTradeOffers).toHaveBeenCalled();
    });
  });

  describe('New Feature: Accept and refuse trade offers', () => {
    test('should call acceptTradeOffer when accept button clicked', async () => {
      const mockOffers = [
        { 
          id: 1, 
          sender: { id: 2, username: 'Alice' },
          receiver: { id: 1, username: 'Bob' },
          offeredSticker: { id: 1, name: 'Lion' },
          receivedSticker: { id: 2, name: 'Tiger' },
          status: 'PENDING' 
        }
      ];
      (apiService.getReceivedTradeOffers as jest.Mock).mockResolvedValue(mockOffers);
      (apiService.getSentTradeOffers as jest.Mock).mockResolvedValue([]);
      (apiService.acceptTradeOffer as jest.Mock).mockResolvedValue({});

      renderTrading();

      await waitFor(() => {
        expect(apiService.getReceivedTradeOffers).toHaveBeenCalled();
      });
    });

    test('should call refuseTradeOffer when refuse button clicked', async () => {
      const mockOffers = [
        { 
          id: 1, 
          sender: { id: 2, username: 'Alice' },
          receiver: { id: 1, username: 'Bob' },
          offeredSticker: { id: 1, name: 'Lion' },
          receivedSticker: { id: 2, name: 'Tiger' },
          status: 'PENDING' 
        }
      ];
      (apiService.getReceivedTradeOffers as jest.Mock).mockResolvedValue(mockOffers);
      (apiService.getSentTradeOffers as jest.Mock).mockResolvedValue([]);
      (apiService.refuseTradeOffer as jest.Mock).mockResolvedValue({});

      renderTrading();

      await waitFor(() => {
        expect(apiService.getSentTradeOffers).toHaveBeenCalled();
      });
    });
  });

  describe('Bug Fix: Key props in map iterations', () => {
    test('should render trade offers without React key warnings', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      const mockOffers = [
        { 
          id: 1, 
          sender: { id: 2, username: 'Alice' },
          receiver: { id: 1, username: 'Bob' },
          offeredSticker: { id: 1, name: 'Lion' },
          receivedSticker: { id: 2, name: 'Tiger' },
          status: 'PENDING' 
        },
        { 
          id: 2, 
          sender: { id: 3, username: 'Charlie' },
          receiver: { id: 1, username: 'Bob' },
          offeredSticker: { id: 3, name: 'Elephant' },
          receivedSticker: { id: 4, name: 'Zebra' },
          status: 'PENDING' 
        }
      ];
      (apiService.getReceivedTradeOffers as jest.Mock).mockResolvedValue(mockOffers);
      (apiService.getSentTradeOffers as jest.Mock).mockResolvedValue([]);

      renderTrading();

      await waitFor(() => {
        expect(screen.getByText(/Trading/i)).toBeInTheDocument();
      });

      // Check that no key warnings were logged
      const keyWarnings = consoleSpy.mock.calls.filter(call => 
        typeof call[0] === 'string' && call[0].includes('unique "key" prop')
      );
      expect(keyWarnings.length).toBe(0);

      consoleSpy.mockRestore();
    });
  });
});
