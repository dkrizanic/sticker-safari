import React, { useState } from 'react';
import { useUser } from '../context/UserContext';
import { useNavigate } from "react-router-dom";
import apiService from '../api/apiService';


const Login = (): JSX.Element => {
    const { login } = useUser();
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string>('');
    const [isRegistering, setIsRegistering] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async () => {
        if (!username.trim()) {
            setError('Please enter a username');
            return;
        }

        if (!password.trim()) {
            setError('Please enter a password');
            return;
        }

        setLoading(true);
        setError('');

        // Store password temporarily for authentication
        sessionStorage.setItem('password', password);

        try {
            // Try to get collector data first (for regular users)
            const collector = await apiService.getCollectorByUsername(username);
            
            // Test authentication by calling a protected endpoint
            try {
                await apiService.getNotifications(collector.id);
            } catch (authError: any) {
                // If we get 401, credentials are wrong
                if (authError.response?.status === 401) {
                    sessionStorage.removeItem('password');
                    setError('Invalid username or password.');
                    setLoading(false);
                    return;
                }
                // Other errors (like no notifications) are okay - auth worked
            }
            
            // Authentication successful for collector
            login({ id: collector.id, username: collector.username, role: 'COLLECTOR' });
            navigate('/collection');
            
        } catch (collectorError: any) {
            // If collector not found (any error), try admin login
            try {
                // Store username temporarily for auth test
                localStorage.setItem('username', username);
                
                // Test admin authentication by calling an admin endpoint
                await apiService.getAllAlbums();
                
                // Authentication successful for admin
                login({ username: username, role: 'ADMIN' });
                navigate('/admin');
                
            } catch (adminError: any) {
                console.error('Admin login failed:', adminError);
                localStorage.removeItem('username');
                sessionStorage.removeItem('password');
                if (adminError.response?.status === 401 || adminError.response?.status === 403) {
                    setError('Invalid username or password.');
                } else {
                    setError('Login failed. Please try again.');
                }
            }
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async () => {
        if (!username.trim()) {
            setError('Please enter a username');
            return;
        }

        if (username.trim().length < 3) {
            setError('Username must be at least 3 characters long');
            return;
        }

        if (!password.trim()) {
            setError('Please enter a password');
            return;
        }

        if (password.length < 6) {
            setError('Password must be at least 6 characters long');
            return;
        }

        setLoading(true);
        setError('');

        try {
            // For new users, default password is "collector"
            sessionStorage.setItem('password', 'collector');
            
            const collector = await apiService.createCollector(username);
            login({ id: collector.id, username: collector.username });
            navigate('/collection');
        } catch (err: any) {
            console.error('Registration failed:', err);
            sessionStorage.removeItem('password');
            const errorMessage = err.response?.data?.message || 'Registration failed. Username might already exist.';
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = () => {
        if (isRegistering) {
            handleRegister();
        } else {
            handleLogin();
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            handleSubmit();
        }
    };

    return (
        <div className="flex items-center justify-center h-screen bg-gray-100">
            <div className="bg-white p-8 rounded shadow-md w-96">
                <h2 className="text-2xl font-bold mb-4">
                    {isRegistering ? 'Create Account' : 'Login'}
                </h2>
                {error && (
                    <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
                        {error}
                    </div>
                )}
                <label className="block mb-4">
                    <span className="text-gray-700">Username:</span>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        onKeyPress={handleKeyPress}
                        className="w-full mt-1 p-2 border rounded"
                        disabled={loading}
                        placeholder="Enter your username"
                    />
                </label>
                <label className="block mb-4">
                    <span className="text-gray-700">Password:</span>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        onKeyPress={handleKeyPress}
                        className="w-full mt-1 p-2 border rounded"
                        disabled={loading}
                        placeholder="Enter your password"
                    />
                </label>
                <button
                    onClick={handleSubmit}
                    disabled={loading}
                    className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 focus:outline-none focus:shadow-outline-blue disabled:bg-gray-400 mb-3"
                >
                    {loading 
                        ? (isRegistering ? 'Creating account...' : 'Logging in...') 
                        : (isRegistering ? 'Create Account' : 'Login')
                    }
                </button>
                <div className="text-center">
                    <button
                        onClick={() => {
                            setIsRegistering(!isRegistering);
                            setError('');
                            setPassword('');
                        }}
                        disabled={loading}
                        className="text-blue-500 hover:text-blue-700 text-sm disabled:text-gray-400"
                    >
                        {isRegistering 
                            ? 'Already have an account? Login' 
                            : "Don't have an account? Register"
                        }
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Login;