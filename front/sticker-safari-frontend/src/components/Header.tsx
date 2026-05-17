import React from 'react';
import {Link, useNavigate, useLocation} from "react-router-dom";
import {useUser} from '../context/UserContext';
import NotificationCenter from './NotificationCenter';


const Header = (): JSX.Element => {
    const {currentUser, logout} = useUser();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const isActive = (path: string) => location.pathname === path;

    return (
        <header className="bg-white border-b border-gray-200 shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    {/* Logo/Brand */}
                    <div className="flex items-center">
                        <Link to={currentUser?.role === 'ADMIN' ? '/admin' : '/collection'} className="flex items-center space-x-2">
                            <span className="text-3xl">🦁</span>
                            <span className="text-xl font-bold text-gray-900">Sticker Safari</span>
                        </Link>
                    </div>

                    {/* Navigation Links */}
                    {currentUser && currentUser.role === 'COLLECTOR' && (
                        <nav className="hidden md:flex space-x-1">
                            <Link 
                                to="/collection" 
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                    isActive('/collection')
                                        ? 'bg-blue-50 text-blue-600'
                                        : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                                }`}
                            >
                                📚 Collection
                            </Link>
                            <Link 
                                to="/trading" 
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                    isActive('/trading')
                                        ? 'bg-green-50 text-green-600'
                                        : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                                }`}
                            >
                                🔄 Trading
                            </Link>
                            <Link 
                                to="/pack-opening" 
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                    isActive('/pack-opening')
                                        ? 'bg-purple-50 text-purple-600'
                                        : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                                }`}
                            >
                                📦 Pack Opening
                            </Link>
                            <Link 
                                to="/leaderboard" 
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                    isActive('/leaderboard')
                                        ? 'bg-yellow-50 text-yellow-600'
                                        : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                                }`}
                            >
                                🏆 Leaderboard
                            </Link>
                        </nav>
                    )}

                    {currentUser && currentUser.role === 'ADMIN' && (
                        <nav className="hidden md:flex space-x-1">
                            <Link 
                                to="/admin" 
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                    isActive('/admin')
                                        ? 'bg-purple-50 text-purple-600'
                                        : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                                }`}
                            >
                                📊 Dashboard
                            </Link>
                        </nav>
                    )}

                    {/* Right Side - User Info & Actions */}
                    <div className="flex items-center space-x-3">
                        {currentUser && currentUser.role === 'COLLECTOR' && (
                            <>
                                <NotificationCenter />
                                
                                {/* Stars Display */}
                                <div className="flex items-center gap-1.5 px-3 py-1.5 bg-gradient-to-r from-yellow-50 to-amber-50 border border-yellow-200 rounded-full">
                                    <span className="text-yellow-500 text-base">⭐</span>
                                    <span className="font-semibold text-gray-800 text-sm">{currentUser.stars || 0}</span>
                                </div>
                                
                                {/* Profile Link */}
                                <Link 
                                    to="/profile" 
                                    className="flex items-center gap-2 px-3 py-1.5 hover:bg-gray-100 rounded-lg transition-colors"
                                    title="Go to profile"
                                >
                                    <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center">
                                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                        </svg>
                                    </div>
                                    <span className="font-medium text-gray-700 text-sm hidden lg:block">{currentUser.username}</span>
                                </Link>
                                
                                {/* Logout Button */}
                                <button 
                                    onClick={handleLogout}
                                    className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
                                    title="Logout"
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                                    </svg>
                                </button>
                            </>
                        )}
                        
                        {currentUser && currentUser.role === 'ADMIN' && (
                            <>
                                <span className="font-medium text-gray-700 text-sm">{currentUser.username}</span>
                                <button 
                                    onClick={handleLogout}
                                    className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg transition-colors"
                                >
                                    Logout
                                </button>
                            </>
                        )}
                        
                        {!currentUser && (
                            <Link 
                                to="/login" 
                                className="px-4 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg transition-colors"
                            >
                                Login
                            </Link>
                        )}
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Header;