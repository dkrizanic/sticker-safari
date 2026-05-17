import React, { useState, useEffect } from 'react';
import { useUser } from '../context/UserContext';
import apiService from '../api/apiService';

interface Notification {
    id: number;
    message: string;
    type: string;
    read: boolean;
    createdAt: string;
}

const NotificationCenter: React.FC = () => {
    const { currentUser } = useUser();
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [isOpen, setIsOpen] = useState(false);
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        if (currentUser?.id) {
            fetchNotifications();
        }
    }, [currentUser]);

    const fetchNotifications = async () => {
        if (!currentUser?.id) return;

        try {
            const data = await apiService.getNotifications(currentUser.id);
            setNotifications(data);
            setUnreadCount(data.filter((n: Notification) => !n.read).length);
        } catch (error) {
            console.error('Failed to fetch notifications:', error);
        }
    };

    const markAsRead = async (notificationId: number) => {
        try {
            await apiService.markNotificationAsRead(notificationId);
            setNotifications(prev =>
                prev.map(n => n.id === notificationId ? { ...n, read: true } : n)
            );
            setUnreadCount(prev => Math.max(0, prev - 1));
        } catch (error) {
            console.error('Failed to mark notification as read:', error);
        }
    };

    const markAllAsRead = async () => {
        if (!currentUser?.id) return;

        try {
            for (const notification of notifications.filter(n => !n.read)) {
                await apiService.markNotificationAsRead(notification.id);
            }
            setNotifications(prev => prev.map(n => ({ ...n, read: true })));
            setUnreadCount(0);
        } catch (error) {
            console.error('Failed to mark all as read:', error);
        }
    };

    const getNotificationIcon = (type: string) => {
        switch (type) {
            case 'TRADE_ACCEPTED':
                return '✓';
            case 'TRADE_REFUSED':
                return '✕';
            case 'TRADE_RECEIVED':
                return '↔';
            case 'PACK_OPENED':
                return '📦';
            default:
                return '•';
        }
    };

    const getNotificationColor = (type: string) => {
        switch (type) {
            case 'TRADE_ACCEPTED':
                return 'text-green-600 bg-green-50';
            case 'TRADE_REFUSED':
                return 'text-red-600 bg-red-50';
            case 'TRADE_RECEIVED':
                return 'text-blue-600 bg-blue-50';
            case 'PACK_OPENED':
                return 'text-purple-600 bg-purple-50';
            default:
                return 'text-gray-600 bg-gray-50';
        }
    };

    if (!currentUser) return null;

    return (
        <div className="relative">
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="relative p-2 text-gray-600 hover:text-gray-900 transition-colors"
            >
                <svg
                    className="w-6 h-6"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                >
                    <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                    />
                </svg>
                {unreadCount > 0 && (
                    <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-red-600 rounded-full">
                        {unreadCount}
                    </span>
                )}
            </button>

            {isOpen && (
                <>
                    <div
                        className="fixed inset-0 z-40"
                        onClick={() => setIsOpen(false)}
                    ></div>
                    <div className="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-xl border border-gray-200 z-50 max-h-[500px] overflow-hidden flex flex-col">
                        <div className="p-4 border-b border-gray-200 flex justify-between items-center bg-gradient-to-r from-blue-500 to-purple-600 text-white">
                            <h3 className="text-lg font-semibold">Notifications</h3>
                            {unreadCount > 0 && (
                                <button
                                    onClick={markAllAsRead}
                                    className="text-sm hover:underline"
                                >
                                    Mark all read
                                </button>
                            )}
                        </div>
                        <div className="overflow-y-auto flex-1">
                            {notifications.length === 0 ? (
                                <div className="p-8 text-center text-gray-500">
                                    <p>No notifications yet</p>
                                </div>
                            ) : (
                                <div className="divide-y divide-gray-100">
                                    {notifications.map((notification) => (
                                        <div
                                            key={notification.id}
                                            className={`p-4 hover:bg-gray-50 transition-colors relative ${
                                                !notification.read ? 'bg-blue-50' : ''
                                            }`}
                                        >
                                            <div className="flex items-start space-x-3">
                                                <div
                                                    className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-lg ${getNotificationColor(
                                                        notification.type
                                                    )}`}
                                                >
                                                    {getNotificationIcon(notification.type)}
                                                </div>
                                                <div 
                                                    className="flex-1 min-w-0 cursor-pointer"
                                                    onClick={() => !notification.read && markAsRead(notification.id)}
                                                >
                                                    <p
                                                        className={`text-sm ${
                                                            !notification.read
                                                                ? 'font-semibold text-gray-900'
                                                                : 'text-gray-700'
                                                        }`}
                                                    >
                                                        {notification.message}
                                                    </p>
                                                    <p className="text-xs text-gray-500 mt-1">
                                                        {new Date(notification.createdAt).toLocaleString()}
                                                    </p>
                                                </div>
                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        setNotifications(prev => prev.filter(n => n.id !== notification.id));
                                                    }}
                                                    className="flex-shrink-0 text-gray-400 hover:text-gray-600 transition-colors"
                                                    title="Remove notification"
                                                >
                                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                                    </svg>
                                                </button>
                                                {!notification.read && (
                                                    <div className="absolute right-12 top-1/2 transform -translate-y-1/2 w-2 h-2 bg-blue-600 rounded-full"></div>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </>
            )}
        </div>
    );
};

export default NotificationCenter;
