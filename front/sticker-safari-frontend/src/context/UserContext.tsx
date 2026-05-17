import React, { createContext, useState, useContext, ReactNode } from 'react';
import { User } from '../types';

interface UserContextType {
  currentUser: User | null;
  login: (user: User) => void;
  logout: () => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

interface UserProviderProps {
  children: ReactNode;
}

export const UserProvider = ({ children }: UserProviderProps): JSX.Element => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);

    const login = (user: User) => {
        setCurrentUser(user);
        localStorage.setItem('username', user.username);
    };

    const logout = () => {
        setCurrentUser(null);
        localStorage.removeItem('username');
        sessionStorage.removeItem('password');
    };

    return (
        <UserContext.Provider value={{ currentUser, login, logout }}>
            {children}
        </UserContext.Provider>
    );
};

export const useUser = (): UserContextType => {
  const context = useContext(UserContext);
  if (context === undefined) {
    throw new Error('useUser must be used within a UserProvider');
  }
  return context;
};