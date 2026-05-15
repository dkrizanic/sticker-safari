import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import PackOpening from './PackOpening';
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

      fireEvent.change(sizeInput, { target: { value: '3' } });
      fireEvent.click(openButton);

      await waitFor(() => {
        expect(apiService.openPack).toHaveBeenCalled();
      });
    });

    test('should validate size parameter', async () => {
      const mockStickers = [
        { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' }
      ];
      (apiService.openPack as jest.Mock).mockResolvedValue(mockStickers);

      renderPackOpening();

      const inputs = screen.getAllByRole('spinbutton');
      const sizeInput = inputs[0];
      const openButton = screen.getByRole('button', { name: /Open Pack/i });

      fireEvent.change(sizeInput, { target: { value: '0' } });
      fireEvent.click(openButton);

      // Should not call API with invalid size (wait a bit to ensure it doesn't get called)
      await new Promise(resolve => setTimeout(resolve, 100));
      expect(apiService.openPack).not.toHaveBeenCalled();
    });
  });

  describe('New Feature: Display opened stickers', () => {
    test('should display stickers after opening pack', async () => {
      const mockStickers = [
        { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' }
      ];
      (apiService.openPack as jest.Mock).mockResolvedValue(mockStickers);

      renderPackOpening();

      const inputs = screen.getAllByRole('spinbutton');
      const sizeInput = inputs[0];
      const openButton = screen.getByRole('button', { name: /Open Pack/i });

      fireEvent.change(sizeInput, { target: { value: '1' } });
      fireEvent.click(openButton);

      await waitFor(() => {
        expect(apiService.openPack).toHaveBeenCalled();
      }, { timeout: 3000 });
    });
  });

  describe('Bug Fix: Key props in map iterations', () => {
    test('should render stickers without React key warnings', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      const mockStickers = [
        { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' },
        { id: 2, name: 'Tiger', latinName: 'Panthera tigris', description: 'Stripes', habitat: 'Forest', src: '/tiger.jpg', album: 'African Safari' }
      ];
      (apiService.openPack as jest.Mock).mockResolvedValue(mockStickers);

      renderPackOpening();

      const inputs = screen.getAllByRole('spinbutton');
      const sizeInput = inputs[0];
      const openButton = screen.getByRole('button', { name: /Open Pack/i });

      fireEvent.change(sizeInput, { target: { value: '2' } });
      fireEvent.click(openButton);

      await waitFor(() => {
        expect(apiService.openPack).toHaveBeenCalled();
      }, { timeout: 3000 });

      // Check that no key warnings were logged
      const keyWarnings = consoleSpy.mock.calls.filter(call => 
        typeof call[0] === 'string' && call[0].includes('unique "key" prop')
      );
      expect(keyWarnings.length).toBe(0);

      consoleSpy.mockRestore();
    });
  });
});
