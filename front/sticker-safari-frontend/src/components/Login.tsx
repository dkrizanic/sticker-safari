import React, { useState } from 'react';
import { useUser } from '../context/UserContext';
import { useNavigate } from "react-router-dom";
import apiService from '../api/apiService';


const Login = (): JSX.Element => {
    const { login } = useUser();
    const [username, setUsername] = useState<string>('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string>('');
    const navigate = useNavigate();

    const handleLogin = async () => {
        if (!username.trim()) {
            setError('Please enter a username');
            return;
        }

        setLoading(true);
        setError('');

        try {
            const collector = await apiService.getCollectorByUsername(username);
            login({ id: collector.id, username: collector.username });
            navigate('/collection');
        } catch (err) {
            console.error('Login failed:', err);
            setError('Login failed. User not found or backend is not running.');
        } finally {
            setLoading(false);
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            handleLogin();
        }
    };

    return (
        <div className="flex items-center justify-center h-screen bg-gray-100">
            <div className="bg-white p-8 rounded shadow-md w-96">
                <h2 className="text-2xl font-bold mb-4">Login</h2>
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
                <button
                    onClick={handleLogin}
                    disabled={loading}
                    className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 focus:outline-none focus:shadow-outline-blue disabled:bg-gray-400"
                >
                    {loading ? 'Logging in...' : 'Login'}
                </button>
            </div>
        </div>
    );
};

export default Login;