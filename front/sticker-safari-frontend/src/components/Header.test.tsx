import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Header from './Header';

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

// Mock useUser hook - will be overridden per test
let mockCurrentUser: any = null;
jest.mock('../context/UserContext', () => ({
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

  const renderHeader = (user = null) => {
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

    test('should NOT show Pack Opening button when not logged in', () => {
      renderHeader(null);
      
      const packOpeningButton = screen.queryByText(/Pack opening/i);
      expect(packOpeningButton).not.toBeInTheDocument();
    });

    test('should show Login button when not logged in', () => {
      renderHeader(null);
      
      const loginButton = screen.getByText(/Login/i);
      expect(loginButton).toBeInTheDocument();
    });
  });

  describe('New Feature: Navigation buttons should appear when logged in', () => {
    const testUser = { id: 1, username: 'Bob', stickers: [] };

    test('should show Collection button when logged in', () => {
      renderHeader(testUser);
      
      const collectionButton = screen.getByText(/Collection/i);
      expect(collectionButton).toBeInTheDocument();
    });

    test('should show Trading button when logged in', () => {
      renderHeader(testUser);
      
      const tradingButton = screen.getByText(/Trading/i);
      expect(tradingButton).toBeInTheDocument();
    });

    test('should show Pack Opening button when logged in', () => {
      renderHeader(testUser);
      
      const packOpeningButton = screen.getByText(/Pack opening/i);
      expect(packOpeningButton).toBeInTheDocument();
    });

    test('should show username when logged in', () => {
      renderHeader(testUser);
      
      const username = screen.getByText('Bob');
      expect(username).toBeInTheDocument();
    });

    test('should show Logout button instead of Login when logged in', () => {
      renderHeader(testUser);
      
      const logoutButton = screen.getByText(/Logout/i);
      expect(logoutButton).toBeInTheDocument();
      
      const loginButton = screen.queryByText(/^Login$/i);
      expect(loginButton).not.toBeInTheDocument();
    });
  });

  describe('Bug Fix: Should use className instead of class attribute', () => {
    test('header should render with proper CSS classes', () => {
      renderHeader(null);
      
      const header = screen.getByRole('banner');
      expect(header).toHaveClass('bg-gray-800', 'text-white');
    });
  });
});
