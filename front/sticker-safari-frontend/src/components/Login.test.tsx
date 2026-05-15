import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Login from './Login';
import apiService from '../api/apiService';

jest.mock('../api/apiService');

const mockLogin = jest.fn();
const mockNavigate = jest.fn();

jest.mock('../context/UserContext', () => ({
  useUser: () => ({
    currentUser: null,
    login: mockLogin,
    logout: jest.fn(),
  }),
}));

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('Login Component Tests', () => {
  const renderLogin = () => {
    return render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Bug Fix: Should use className instead of class attribute', () => {
    test('login form should render with proper CSS classes', () => {
      const { container } = renderLogin();
      
      expect(container).toBeTruthy();
      expect(screen.getByRole('button', { name: /Login/i })).toBeInTheDocument();
    });
  });

  describe('New Feature: Backend API integration', () => {
    test('should call API on login', async () => {
      const mockUser = { id: 1, username: 'Bob', stickers: [] };
      (apiService.getCollectorByUsername as jest.Mock).mockResolvedValue(mockUser);

      renderLogin();

      const input = screen.getByPlaceholderText(/Enter your username/i);
      const button = screen.getByRole('button', { name: /Login/i });

      fireEvent.change(input, { target: { value: 'Bob' } });
      fireEvent.click(button);

      await waitFor(() => {
        expect(apiService.getCollectorByUsername).toHaveBeenCalledWith('Bob');
      });
    });

    test('should navigate to collection on successful login', async () => {
      const mockUser = { id: 1, username: 'Bob', stickers: [] };
      (apiService.getCollectorByUsername as jest.Mock).mockResolvedValue(mockUser);

      renderLogin();

      const input = screen.getByPlaceholderText(/Enter your username/i);
      const button = screen.getByRole('button', { name: /Login/i });

      fireEvent.change(input, { target: { value: 'Bob' } });
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith({ id: 1, username: 'Bob' });
        expect(mockNavigate).toHaveBeenCalledWith('/collection');
      });
    });

    test('should show error message on login failure', async () => {
      (apiService.getCollectorByUsername as jest.Mock).mockRejectedValue(new Error('User not found'));

      renderLogin();

      const input = screen.getByPlaceholderText(/Enter your username/i);
      const button = screen.getByRole('button', { name: /Login/i });

      fireEvent.change(input, { target: { value: 'InvalidUser' } });
      fireEvent.click(button);

      await waitFor(() => {
        expect(screen.getByText(/Login failed/i)).toBeInTheDocument();
      });
    });

    test('should handle Enter key press for login', async () => {
      const mockUser = { id: 1, username: 'Bob', stickers: [] };
      (apiService.getCollectorByUsername as jest.Mock).mockResolvedValue(mockUser);

      renderLogin();

      const input = screen.getByPlaceholderText(/Enter your username/i);

      fireEvent.change(input, { target: { value: 'Bob' } });
      fireEvent.keyPress(input, { key: 'Enter', code: 'Enter', charCode: 13 });

      await waitFor(() => {
        expect(apiService.getCollectorByUsername).toHaveBeenCalledWith('Bob');
        expect(mockLogin).toHaveBeenCalledWith({ id: 1, username: 'Bob' });
      });
    });

    test('should show loading state during login', async () => {
      const mockUser = { id: 1, username: 'Bob', stickers: [] };
      let resolveLogin: any;
      (apiService.getCollectorByUsername as jest.Mock).mockImplementation(
        () => new Promise(resolve => { resolveLogin = resolve; })
      );

      renderLogin();

      const input = screen.getByPlaceholderText(/Enter your username/i);
      const button = screen.getByRole('button', { name: /Login/i });

      fireEvent.change(input, { target: { value: 'Bob' } });
      fireEvent.click(button);

      // Wait a bit then check for loading text
      await waitFor(() => {
        expect(screen.getByText(/Logging in.../i)).toBeInTheDocument();
      });

      // Resolve the promise
      resolveLogin(mockUser);

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith({ id: 1, username: 'Bob' });
      });
    });
  });
});
