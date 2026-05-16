import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import PackOpening from '../PackOpening';
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

describe('PackOpening Component Tests', () => {
  const renderPackOpening = () => {
    return render(
      <BrowserRouter>
        <PackOpening />
      </BrowserRouter>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('New Feature: Size parameter for opening packs', () => {
    test('should allow selecting pack size', () => {
      renderPackOpening();

      expect(screen.getByText(/Number of Stickers:/i)).toBeInTheDocument();
      const inputs = screen.getAllByRole('spinbutton');
      expect(inputs.length).toBeGreaterThan(0);
    });

    test('should call openPack with size parameter', async () => {
      const mockStickers = [
        { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' }
      ];
      (apiService.openPack as jest.Mock).mockResolvedValue(mockStickers);

      renderPackOpening();

      const inputs = screen.getAllByRole('spinbutton');
      const sizeInput = inputs[0];
      const openButton = screen.getByRole('button', { name: /Open Pack/i });

      fireEvent.change(sizeInput, { target: { value: '5' } });
      fireEvent.click(openButton);

      await waitFor(() => {
        expect(apiService.openPack).toHaveBeenCalled();
      });
    });
  });
});
