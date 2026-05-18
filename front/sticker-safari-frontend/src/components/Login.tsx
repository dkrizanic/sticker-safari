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
        try {
            // Demo: Use a single login endpoint (simulate)
            if (username === 'admin' && password === 'admin') {
                login({ username, role: 'ADMIN' });
                navigate('/admin');
            } else {
                // For demo, all other users are collectors with password 'collector'
                if (password !== 'collector') {
                    setError('Invalid username or password.');
                } else {
                    // Simulate fetching collector data
                    const collector = await apiService.getCollectorByUsername(username);
                    login({ id: collector.id, username: collector.username, role: 'COLLECTOR', stars: collector.stars || 0 });
                    navigate('/collection');
                }
            }
        } catch (err: any) {
            setError('Invalid username or password.');
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
        setLoading(true);
        setError('');
        try {
            // For demo: registration always uses password 'collector'
            const collector = await apiService.createCollector(username);
            login({ id: collector.id, username: collector.username, role: 'COLLECTOR', stars: collector.stars || 0 });
            navigate('/collection');
        } catch (err: any) {
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
        <div className="min-h-screen bg-gray-100">
            {/* Navigation Header */}
            <nav className="bg-gray-800 text-white py-4 px-6 shadow-lg">
                <div className="max-w-7xl mx-auto flex items-center">
                    <button
                        onClick={() => navigate('/')}
                        className="flex items-center space-x-2 hover:text-gray-300 transition-colors"
                    >
                        <span className="text-2xl">🦁</span>
                        <span className="text-xl font-bold">Sticker Safari</span>
                    </button>
                    <div className="ml-auto">
                        <button
                            onClick={() => navigate('/')}
                            className="px-4 py-2 bg-indigo-500 hover:bg-indigo-600 rounded transition-colors"
                        >
                            ← Back to Home
                        </button>
                    </div>
                </div>
            </nav>
            
            {/* Login Form */}
            <div className="flex items-center justify-center pt-20">
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
                {!isRegistering && (
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
                )}
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
        </div>
    );
};

export default Login;