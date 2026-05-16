import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Header from '../Header';

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

// Mock useUser hook - will be overridden per test
let mockCurrentUser: any = null;
jest.mock('../../context/UserContext', () => ({
  useUser: () => ({
    currentUser: mockCurrentUser,
    login: jest.fn((user) => { mockCurrentUser = user; }),
    logout: jest.fn(() => { mockCurrentUser = null; }),
  }),
}));

describe('Header Component Tests', () => {
  beforeEach(() => {
    mockCurrentUser = null;
  });

  const renderHeader = (user: any = null) => {
    mockCurrentUser = user;
    return render(
      <BrowserRouter>
        <Header />
      </BrowserRouter>
    );
  };

  describe('Bug Fix: Navigation buttons should be hidden when not logged in', () => {
    test('should NOT show Collection button when not logged in', () => {
      renderHeader(null);
      
      const collectionButton = screen.queryByText(/Collection/i);
      expect(collectionButton).not.toBeInTheDocument();
    });

    test('should NOT show Trading button when not logged in', () => {
      renderHeader(null);
      
      const tradingButton = screen.queryByText(/Trading/i);
      expect(tradingButton).not.toBeInTheDocument();
    });
  });

  describe('Feature: Navigation buttons should be visible when logged in', () => {
    test('should show Collection button when logged in', () => {
      const mockUser = { id: 1, username: 'Bob', stickers: [] };
      renderHeader(mockUser);
      
      const collectionButton = screen.getByText(/Collection/i);
      expect(collectionButton).toBeInTheDocument();
    });

    test('should show Trading button when logged in', () => {
      const mockUser = { id: 1, username: 'Bob', stickers: [] };
      renderHeader(mockUser);
      
      const tradingButton = screen.getByText(/Trading/i);
      expect(tradingButton).toBeInTheDocument();
    });
  });
});
