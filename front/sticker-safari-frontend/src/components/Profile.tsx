import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '../context/UserContext';
import { useToast } from '../context/ToastContext';
import apiService from '../api/apiService';

const Profile = (): JSX.Element => {
    const { currentUser, logout } = useUser();
    const { addToast } = useToast();
    const navigate = useNavigate();
    
    const [username, setUsername] = useState(currentUser?.username || '');
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [isUpdating, setIsUpdating] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    const handleUpdateUsername = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (!currentUser?.id) {
            addToast('User not found', 'error');
            return;
        }

        if (username.trim() === '') {
            addToast('Username cannot be empty', 'error');
            return;
        }

        if (username === currentUser.username) {
            addToast('Username is the same', 'info');
            return;
        }

        setIsUpdating(true);
        try {
            await apiService.updateCollector(currentUser.id, username);
            
            // Update user context
            const updatedUser = { ...currentUser, username };
            localStorage.setItem('username', username);
            
            addToast('Username updated successfully! 🎉', 'success');
            
            // Refresh page to update context
            window.location.reload();
        } catch (error: any) {
            console.error('Failed to update username:', error);
            addToast(
                error.response?.data?.message || 'Failed to update username',
                'error'
            );
        } finally {
            setIsUpdating(false);
        }
    };

    const handleDeleteProfile = async () => {
        if (!currentUser?.id) {
            addToast('User not found', 'error');
            return;
        }

        setIsDeleting(true);
        try {
            await apiService.deleteCollector(currentUser.id);
            addToast('Profile deleted successfully', 'success');
            
            // Logout and redirect to home
            logout();
            navigate('/');
        } catch (error: any) {
            console.error('Failed to delete profile:', error);
            addToast(
                error.response?.data?.message || 'Failed to delete profile',
                'error'
            );
            setIsDeleting(false);
        }
    };

    if (!currentUser) {
        navigate('/login');
        return <></>;
    }

    return (
        <div className="min-h-screen bg-gray-100 py-8 px-4">
            <div className="max-w-2xl mx-auto">
                <div className="bg-white rounded-lg shadow-md p-6">
                    <h1 className="text-3xl font-bold text-gray-800 mb-6">
                        👤 My Profile
                    </h1>

                    {/* Update Username Section */}
                    <div className="mb-8 pb-8 border-b border-gray-200">
                        <h2 className="text-xl font-semibold text-gray-700 mb-4">
                            Update Username
                        </h2>
                        <form onSubmit={handleUpdateUsername} className="space-y-4">
                            <div>
                                <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
                                    Username
                                </label>
                                <input
                                    id="username"
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    placeholder="Enter new username"
                                    disabled={isUpdating}
                                />
                            </div>
                            <div className="flex gap-3">
                                <button
                                    type="submit"
                                    disabled={isUpdating || username === currentUser.username}
                                    className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                                >
                                    {isUpdating ? 'Updating...' : 'Update Username'}
                                </button>
                                <button
                                    type="button"
                                    onClick={() => navigate(-1)}
                                    className="px-6 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition-colors"
                                >
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </div>

                    {/* Delete Profile Section */}
                    <div>
                        <h2 className="text-xl font-semibold text-red-600 mb-4">
                            ⚠️ Danger Zone
                        </h2>
                        <p className="text-gray-600 mb-4">
                            Once you delete your profile, all your stickers, trade offers, and progress will be permanently lost. This action cannot be undone.
                        </p>
                        
                        {!showDeleteConfirm ? (
                            <button
                                onClick={() => setShowDeleteConfirm(true)}
                                className="px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors"
                            >
                                Delete My Profile
                            </button>
                        ) : (
                            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                                <p className="text-red-800 font-semibold mb-3">
                                    Are you absolutely sure?
                                </p>
                                <p className="text-red-700 mb-4">
                                    This will permanently delete <strong>{currentUser.username}</strong> and all associated data.
                                </p>
                                <div className="flex gap-3">
                                    <button
                                        onClick={handleDeleteProfile}
                                        disabled={isDeleting}
                                        className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                                    >
                                        {isDeleting ? 'Deleting...' : 'Yes, Delete My Profile'}
                                    </button>
                                    <button
                                        onClick={() => setShowDeleteConfirm(false)}
                                        disabled={isDeleting}
                                        className="px-6 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 disabled:bg-gray-200 disabled:cursor-not-allowed transition-colors"
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Profile;
