import React from 'react';
import {Link, useNavigate} from "react-router-dom";
import {useUser} from '../context/UserContext';


const Header = (): JSX.Element => {
    const {currentUser, logout} = useUser();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className="bg-gray-800 text-white py-4 px-4 flex justify-between items-center">
            <div className="flex items-center">
                {currentUser && (
                    <>
                        <Link to="/collection" className="mr-4 px-4 py-2 bg-blue-500 hover:bg-blue-600 rounded">
                            Collection
                        </Link>
                        <Link to="/trading" className="mr-4 px-4 py-2 bg-green-500 hover:bg-green-600 rounded">
                            Trading
                        </Link>
                        <Link to="/pack-opening" className="mr-4 px-4 py-2 bg-blue-500 hover:bg-blue-600 rounded">
                            Pack opening
                        </Link>
                    </>
                )}
            </div>

            <div className="flex flex-row">
                {currentUser && <p className="mr-2 mt-2">{currentUser.username}</p>}
                {currentUser ? <button onClick={handleLogout}
                                       className="px-4 py-2 bg-indigo-500 hover:bg-indigo-600 rounded">Logout</button> :
                    <Link to="/login" className="px-4 py-2 bg-indigo-500 hover:bg-indigo-600 rounded">
                        Login
                    </Link>}
            </div>
        </header>
    );
};

export default Header;