import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Collection from '../Collection';
import apiService from '../../api/apiService';

jest.mock('../../api/apiService');

// Mock useUser hook directly
jest.mock('../../context/UserContext', () => ({
  useUser: () => ({
    currentUser: { id: 1, username: 'Bob' },
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

  test('renders the Collection component with albums', async () => {
    const mockAlbum1 = {
      id: 1,
      name: 'African Safari',
      stickers: [
        { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg' },
        { id: 2, name: 'Tiger', latinName: 'Panthera tigris', description: 'Stripes', habitat: 'Forest', src: '/tiger.jpg' }
      ]
    };

    const mockAlbum2 = {
      id: 2,
      name: 'Asian Jungle',
      stickers: [
        { id: 3, name: 'Panda', latinName: 'Ailuropoda melanoleuca', description: 'Black and White', habitat: 'Forest', src: '/panda.jpg' }
      ]
    };

    (apiService.getAlbum as jest.Mock).mockImplementation((id) => {
      if (id === 1) return Promise.resolve(mockAlbum1);
      if (id === 2) return Promise.resolve(mockAlbum2);
    });

    renderCollection();

    await waitFor(() => {
      expect(apiService.getAlbum).toHaveBeenCalledWith(1);
      expect(apiService.getAlbum).toHaveBeenCalledWith(2);
    });
  });
});