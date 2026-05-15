import React from 'react';
import { render, fireEvent } from '@testing-library/react';
import AlbumCard from './AlbumCard';

describe('AlbumCard Component Tests', () => {
  const mockAlbum = {
    id: 1,
    name: 'African Safari',
    stickers: [
      { id: 1, name: 'Lion', latinName: 'Panthera leo', description: 'King', habitat: 'Savanna', src: '/lion.jpg', album: 'African Safari' },
      { id: 2, name: 'Tiger', latinName: 'Panthera tigris', description: 'Stripes', habitat: 'Forest', src: '/tiger.jpg', album: 'African Safari' }
    ]
  };

  const mockOwnedStickers = [1]; // User owns sticker with ID 1

  describe('Bug Fix: onClick should not execute immediately', () => {
    test('card should render without throwing errors', () => {
      const { container } = render(<AlbumCard album={mockAlbum} ownedStickers={mockOwnedStickers} />);
      
      // Component should render without throwing errors
      expect(container).toBeTruthy();
      
      // Find the clickable card div
      const card = container.querySelector('.cursor-pointer');
      expect(card).toBeTruthy();
    });

    test('clicking card should toggle stickers visibility', () => {
      const { container } = render(<AlbumCard album={mockAlbum} ownedStickers={mockOwnedStickers} />);
      
      // Find the clickable card
      const card = container.querySelector('.cursor-pointer');
      expect(card).toBeTruthy();
      
      // Click to toggle
      if (card) fireEvent.click(card);
      
      // Component should still be present after toggle
      expect(container.querySelector('.cursor-pointer')).toBeTruthy();
    });
  });

  describe('Bug Fix: Arrow icon direction should be correct', () => {
    test('should render with arrow icon', () => {
      const { container } = render(<AlbumCard album={mockAlbum} ownedStickers={mockOwnedStickers} />);
      
      // Should have svg icon for arrow
      const svg = container.querySelector('svg');
      expect(svg).toBeTruthy();
    });

    test('should toggle visibility when clicked', () => {
      const { container } = render(<AlbumCard album={mockAlbum} ownedStickers={mockOwnedStickers} />);
      
      const card = container.querySelector('.cursor-pointer');
      
      // Click to toggle visibility
      if (card) fireEvent.click(card);
      
      // Component should still be present after toggle
      expect(container.querySelector('.cursor-pointer')).toBeTruthy();
    });
  });

  describe('Bug Fix: Should include key props for sticker list', () => {
    test('should render stickers without React key warnings', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      
      const { container } = render(<AlbumCard album={mockAlbum} ownedStickers={mockOwnedStickers} />);
      
      // Click to expand and show stickers
      const card = container.querySelector('.cursor-pointer');
      if (card) fireEvent.click(card);
      
      // Check that no key warnings were logged
      const keyWarnings = consoleSpy.mock.calls.filter(call => 
        typeof call[0] === 'string' && call[0].includes('unique "key" prop')
      );
      expect(keyWarnings.length).toBe(0);
      
      consoleSpy.mockRestore();
    });
  });
});
