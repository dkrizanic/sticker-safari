import React, { useState } from 'react';
import { useUser } from '../context/UserContext';
import { useNavigate } from "react-router-dom";


const Login = (): JSX.Element => {
    const { login } = useUser();
    const [username, setUsername] = useState<string>('');
    const navigate = useNavigate();

    const handleLogin = () => {
        login({ username });
        navigate('/collection');
    };

    return (
        <div class="flex items-center justify-center h-screen bg-gray-100">
            <div class="bg-white p-8 rounded shadow-md">
                <h2 class="text-2xl font-bold mb-4">Login</h2>
                <label class="block mb-2">
                    <span class="text-gray-700">Username:</span>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        class="w-full mt-1 p-2 border rounded"
                    />
                </label>
                <button
                    onClick={handleLogin}
                    class="bg-blue-500 text-white p-2 rounded hover:bg-blue-600 focus:outline-none focus:shadow-outline-blue"
                >
                    Login
                </button>
            </div>
    );
};

export default Login;