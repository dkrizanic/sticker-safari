import React from 'react';
import { useUser } from '../context/UserContext';
import { useNavigate } from 'react-router-dom';

const AdminDashboard: React.FC = () => {
    const { currentUser } = useUser();
    const navigate = useNavigate();

    // Redirect if not admin
    React.useEffect(() => {
        if (!currentUser || currentUser.role !== 'ADMIN') {
            navigate('/login');
        }
    }, [currentUser, navigate]);

    if (!currentUser || currentUser.role !== 'ADMIN') {
        return null;
    }

    return (
        <div className="min-h-screen bg-gray-100 py-8 px-4">
            <div className="max-w-7xl mx-auto">
                <div className="bg-white rounded-lg shadow-lg p-8">
                    <h1 className="text-4xl font-bold text-gray-800 mb-6">
                        Admin Dashboard
                    </h1>
                    
                    <div className="mb-8">
                        <p className="text-lg text-gray-600 mb-4">
                            Welcome, <span className="font-semibold text-purple-600">{currentUser.username}</span>!
                        </p>
                        <p className="text-gray-600">
                            You have administrative access to manage albums, stickers, and collectors.
                        </p>
                    </div>

                    <div className="grid md:grid-cols-3 gap-6">
                        {/* Albums Management */}
                        <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-6 text-white hover:shadow-xl transition-shadow">
                            <div className="text-4xl mb-4">📚</div>
                            <h2 className="text-2xl font-bold mb-2">Albums</h2>
                            <p className="text-blue-100 mb-4">
                                Create, update, and delete album collections
                            </p>
                            <div className="flex flex-col space-y-2">
                                <button 
                                    onClick={() => navigate('/admin/albums')}
                                    className="bg-white text-blue-600 px-4 py-2 rounded-lg font-semibold hover:bg-blue-50 transition-colors"
                                >
                                    Manage Albums
                                </button>
                            </div>
                        </div>

                        {/* Stickers Management */}
                        <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-6 text-white hover:shadow-xl transition-shadow">
                            <div className="text-4xl mb-4">🦁</div>
                            <h2 className="text-2xl font-bold mb-2">Stickers</h2>
                            <p className="text-green-100 mb-4">
                                Add, update, and manage sticker content
                            </p>
                            <div className="flex flex-col space-y-2">
                                <button 
                                    onClick={() => navigate('/admin/stickers')}
                                    className="bg-white text-green-600 px-4 py-2 rounded-lg font-semibold hover:bg-green-50 transition-colors"
                                >
                                    Manage Stickers
                                </button>
                            </div>
                        </div>

                        {/* Collectors Management */}
                        <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-xl p-6 text-white hover:shadow-xl transition-shadow">
                            <div className="text-4xl mb-4">👥</div>
                            <h2 className="text-2xl font-bold mb-2">Collectors</h2>
                            <p className="text-purple-100 mb-4">
                                View and manage collector accounts
                            </p>
                            <div className="flex flex-col space-y-2">
                                <button 
                                    onClick={() => navigate('/admin/collectors')}
                                    className="bg-white text-purple-600 px-4 py-2 rounded-lg font-semibold hover:bg-purple-50 transition-colors"
                                >
                                    Manage Collectors
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
