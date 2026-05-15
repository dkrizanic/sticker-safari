import { render, screen } from '@testing-library/react';
import App from './App';

test('renders Sticker Safari app', () => {
  render(<App />);
  // App should render the Header with Login button
  const loginButton = screen.getByText(/Login/i);
  expect(loginButton).toBeInTheDocument();
});
