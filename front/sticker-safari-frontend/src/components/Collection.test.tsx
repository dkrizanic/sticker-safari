import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Collection from './Collection';
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

describe('Collection Component Tests', () => {
  const renderCollection = () => {
    return render(
      <BrowserRouter>
        <Collection />
      </BrowserRouter>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('New Feature: Backend API integration', () => {
    test('should fetch albums from backend on mount', async () => {
      const mockAlbum = {
        id: 1,
        name: 'African Safari',
        stickers: [
          { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King of jungle', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' }
        ]
      };
      (apiService.getAlbum as jest.Mock).mockResolvedValue(mockAlbum);

      renderCollection();

      await waitFor(() => {
        expect(apiService.getAlbum).toHaveBeenCalledWith(1);
        expect(apiService.getAlbum).toHaveBeenCalledWith(2);
      });
    });

    test('should display album data from backend', async () => {
      const mockAlbum = {
        id: 1,
        name: 'African Safari',
        stickers: [
          { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King of jungle', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' }
        ]
      };
      (apiService.getAlbum as jest.Mock).mockResolvedValue(mockAlbum);

      renderCollection();

      await waitFor(() => {
        expect(screen.getAllByText(/African Safari/i).length).toBeGreaterThan(0);
      });
    });
  });

  describe('Bug Fix: Should cast album response to AlbumResponse type', () => {
    test('should handle album data without type errors', async () => {
      const mockAlbum = {
        id: 1,
        name: 'African Safari',
        stickers: [
          { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' }
        ]
      };
      (apiService.getAlbum as jest.Mock).mockResolvedValue(mockAlbum);

      const { container } = renderCollection();

      await waitFor(() => {
        expect(container).toBeTruthy();
        expect(apiService.getAlbum).toHaveBeenCalled();
      });
    });
  });

  describe('Bug Fix: Key props in map iterations', () => {
    test('should render albums without React key warnings', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      const mockAlbum = {
        id: 1,
        name: 'African Safari',
        stickers: [
          { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' },
          { id: 2, name: 'Tiger', latinName: 'Panthera tigris', description: 'Stripes', habitat: 'Forest', src: '/tiger.jpg', album: 'African Safari' }
        ]
      };
      (apiService.getAlbum as jest.Mock).mockResolvedValue(mockAlbum);

      renderCollection();

      await waitFor(() => {
        expect(screen.getAllByText(/African Safari/i).length).toBeGreaterThan(0);
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
