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
                {currentUser && <p className="font-medium">{currentUser.username}</p>}
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