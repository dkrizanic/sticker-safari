import React, { createContext, useState, useContext, ReactNode, useEffect } from 'react';
import { User } from '../types';
import apiService from '../api/apiService';

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
  const [isRestoring, setIsRestoring] = useState(true);

    const login = (user: User) => {
        setCurrentUser(user);
        localStorage.setItem('username', user.username);
        if (user.role === 'COLLECTOR' && user.id) {
            localStorage.setItem('userId', user.id.toString());
        }
        if (user.role) {
            localStorage.setItem('userRole', user.role);
        }
    };

    const logout = () => {
        setCurrentUser(null);
        localStorage.removeItem('username');
        localStorage.removeItem('userId');
        localStorage.removeItem('userRole');
        sessionStorage.removeItem('password');
    };

    // Restore session on mount
    useEffect(() => {
        const restoreSession = async () => {
            const username = localStorage.getItem('username');
            const password = sessionStorage.getItem('password');
            const userRole = localStorage.getItem('userRole');
            const userId = localStorage.getItem('userId');

            if (!username || !password) {
                setIsRestoring(false);
                return;
            }

            try {
                if (userRole === 'ADMIN') {
                    // Verify admin session by calling an admin endpoint
                    await apiService.getAllAlbums();
                    setCurrentUser({ username, role: 'ADMIN' });
                } else if (userRole === 'COLLECTOR' && userId) {
                    // Verify collector session
                    const collector = await apiService.getCollectorByUsername(username);
                    await apiService.getNotifications(collector.id);
                    setCurrentUser({ id: collector.id, username: collector.username, role: 'COLLECTOR' });
                }
            } catch (error) {
                console.error('Session restoration failed:', error);
                // Clear invalid session
                logout();
            } finally {
                setIsRestoring(false);
            }
        };

        restoreSession();
    }, []);

    // Don't render children until session restoration is complete
    if (isRestoring) {
        return <div>Loading...</div>;
    }

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