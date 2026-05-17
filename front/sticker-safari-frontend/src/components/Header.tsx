import React from 'react';
import {Link, useNavigate} from "react-router-dom";
import {useUser} from '../context/UserContext';
import NotificationCenter from './NotificationCenter';


const Header = (): JSX.Element => {
    const {currentUser, logout} = useUser();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className="bg-gray-800 text-white py-4 px-4 flex justify-between items-center shadow-lg">
            <div className="flex items-center">
                {currentUser && currentUser.role === 'COLLECTOR' && (
                    <>
                        <Link to="/collection" className="mr-4 px-4 py-2 bg-blue-500 hover:bg-blue-600 rounded transition-colors">
                            Collection
                        </Link>
                        <Link to="/trading" className="mr-4 px-4 py-2 bg-green-500 hover:bg-green-600 rounded transition-colors">
                            Trading
                        </Link>
                        <Link to="/pack-opening" className="mr-4 px-4 py-2 bg-purple-500 hover:bg-purple-600 rounded transition-colors">
                            Pack opening
                        </Link>
                    </>
                )}
                {currentUser && currentUser.role === 'ADMIN' && (
                    <>
                        <Link to="/admin" className="mr-4 px-4 py-2 bg-purple-500 hover:bg-purple-600 rounded transition-colors">
                            Dashboard
                        </Link>
                    </>
                )}
            </div>

            <div className="flex flex-row items-center space-x-4">
                {currentUser && currentUser.role === 'COLLECTOR' && <NotificationCenter />}
                {currentUser && currentUser.role === 'COLLECTOR' && (
                    <Link 
                        to="/profile" 
                        className="flex items-center gap-2 px-3 py-1 hover:bg-gray-700 rounded transition-colors cursor-pointer"
                        title="Go to profile"
                    >
                        <button className="text-gray-300 hover:text-white">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                            </svg>
                        </button>
                        <p className="font-medium">{currentUser.username}</p>
                    </Link>
                )}
                {currentUser && currentUser.role === 'ADMIN' && (
                    <p className="font-medium">{currentUser.username}</p>
                )}
                {currentUser ? <button onClick={handleLogout}
                                       className="px-4 py-2 bg-indigo-500 hover:bg-indigo-600 rounded transition-colors">Logout</button> :
                    <Link to="/login" className="px-4 py-2 bg-indigo-500 hover:bg-indigo-600 rounded transition-colors">
                        Login
                    </Link>}
            </div>
        </header>
    );
};

export default Header;