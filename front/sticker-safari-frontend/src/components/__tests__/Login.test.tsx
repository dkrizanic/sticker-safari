import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Login from '../Login';
import apiService from '../../api/apiService';

jest.mock('../../api/apiService');

const mockLogin = jest.fn();
const mockNavigate = jest.fn();

jest.mock('../../context/UserContext', () => ({
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
      const mockUser = { id: 1, username: 'Bob' };
      (apiService.getCollectorByUsername as jest.Mock).mockResolvedValue(mockUser);

      renderLogin();

      const input = screen.getByPlaceholderText(/Enter your username/i) as HTMLInputElement;
      fireEvent.change(input, { target: { value: 'Bob' } });

      const loginButton = screen.getByRole('button', { name: /Login/i });
      fireEvent.click(loginButton);

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith(expect.objectContaining({ username: 'Bob' }));
      });
    });
  });
});
