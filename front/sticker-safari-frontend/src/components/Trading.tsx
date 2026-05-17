import React, { useState, useEffect } from 'react';
import apiService, { TradeOfferResponse, CollectorResponse, StickerWithQuantity } from '../api/apiService';
import { useUser } from '../context/UserContext';
import { useToast } from '../context/ToastContext';

const Trading = (): JSX.Element => {
    const { currentUser } = useUser();
    const { addToast } = useToast();
    const [receivedOffers, setReceivedOffers] = useState<TradeOfferResponse[]>([]);
    const [sentOffers, setSentOffers] = useState<TradeOfferResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState<'received' | 'sent'>('received');
    
    // Create trade modal state
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [collectors, setCollectors] = useState<CollectorResponse[]>([]);
    const [selectedReceiver, setSelectedReceiver] = useState<number | null>(null);
    const [myStickers, setMyStickers] = useState<StickerWithQuantity[]>([]);
    const [receiverStickers, setReceiverStickers] = useState<StickerWithQuantity[]>([]);
    const [selectedOfferedSticker, setSelectedOfferedSticker] = useState<number | null>(null);
    const [selectedRequestedSticker, setSelectedRequestedSticker] = useState<number | null>(null);
    const [tradeMessage, setTradeMessage] = useState('');

    useEffect(() => {
        if (currentUser?.id) {
            fetchTradeOffers();
        }
    }, [currentUser]);

    const fetchTradeOffers = async () => {
        if (!currentUser?.id) return;

        setLoading(true);
        try {
            const [received, sent] = await Promise.all([
                apiService.getReceivedTradeOffers(currentUser.id),
                apiService.getSentTradeOffers(currentUser.id)
            ]);
            setReceivedOffers(received);
            setSentOffers(sent);
        } catch (error) {
            console.error('Failed to fetch trade offers:', error);
            addToast('Failed to load trade offers', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleAcceptOffer = async (offer: TradeOfferResponse) => {
        if (!currentUser?.id) return;

        try {
            await apiService.acceptTradeOffer({
                senderId: offer.sender.id,
                receiverId: currentUser.id,
                offeredStickerId: offer.offeredSticker.id,
                receivedStickerId: offer.receivedSticker.id
            });
            addToast(`Trade accepted! You received ${offer.offeredSticker.name} 🎉`, 'success');
            fetchTradeOffers();
        } catch (error) {
            console.error('Failed to accept trade offer:', error);
            addToast('Failed to accept trade offer', 'error');
        }
    };

    const handleRefuseOffer = async (offer: TradeOfferResponse) => {
        if (!currentUser?.id) return;

        try {
            await apiService.refuseTradeOffer({
                senderId: offer.sender.id,
                receiverId: currentUser.id,
                offeredStickerId: offer.offeredSticker.id,
                receivedStickerId: offer.receivedSticker.id
            });
            addToast('Trade offer refused', 'info');
            fetchTradeOffers();
        } catch (error) {
            console.error('Failed to refuse trade offer:', error);
            addToast('Failed to refuse trade offer', 'error');
        }
    };

    const handleCancelOffer = async (offer: TradeOfferResponse) => {
        if (!currentUser?.id) return;

        try {
            await apiService.cancelTradeOffer(
                offer.sender.id,
                offer.receiver.id,
                offer.offeredSticker.id,
                offer.receivedSticker.id
            );
            addToast('Trade offer canceled', 'info');
            fetchTradeOffers();
        } catch (error) {
            console.error('Failed to cancel trade offer:', error);
            addToast('Failed to cancel trade offer', 'error');
        }
    };

    const handleOpenCreateModal = async () => {
        setShowCreateModal(true);
        setLoading(true);
        try {
            const allCollectors = await apiService.getAllCollectors();
            // Filter out current user from collectors list
            setCollectors(allCollectors.filter(c => c.id !== currentUser?.id));
            
            // Load current user's stickers with quantities
            if (currentUser?.id) {
                const userCollector = await apiService.getCollector(currentUser.id);
                setMyStickers(userCollector.stickers || []);
            }
        } catch (error) {
            console.error('Failed to load trading data:', error);
            addToast('Failed to load trading data', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleReceiverChange = async (receiverId: number) => {
        setSelectedReceiver(receiverId);
        setSelectedRequestedSticker(null);
        setReceiverStickers([]);
        
        if (!receiverId) return;
        
        setLoading(true);
        try {
            const receiverCollector = await apiService.getCollector(receiverId);
            setReceiverStickers(receiverCollector.stickers || []);
        } catch (error) {
            console.error('Failed to load receiver stickers:', error);
            addToast('Failed to load receiver collection', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleCreateTrade = async () => {
        if (!currentUser?.id || !selectedReceiver || !selectedOfferedSticker || !selectedRequestedSticker) {
            addToast('Please fill all required fields', 'error');
            return;
        }

        setLoading(true);
        try {
            await apiService.sendSpecificTradeOffer({
                senderId: currentUser.id,
                receiverId: selectedReceiver,
                offeredStickerId: selectedOfferedSticker,
                receivedStickerId: selectedRequestedSticker,
                message: tradeMessage || undefined
            });
            addToast('Trade offer sent successfully! 🤝', 'success');
            setShowCreateModal(false);
            resetCreateForm();
            fetchTradeOffers();
        } catch (error: any) {
            console.error('Failed to create trade offer:', error);
            const errorMessage = error.response?.data?.message || 'Failed to create trade offer';
            addToast(errorMessage, 'error');
        } finally {
            setLoading(false);
        }
    };

    const resetCreateForm = () => {
        setSelectedReceiver(null);
        setSelectedOfferedSticker(null);
        setSelectedRequestedSticker(null);
        setTradeMessage('');
        setReceiverStickers([]);
    };

    if (!currentUser) {
        return (
            <div className="py-8 text-center">
                <h2 className="text-2xl font-bold">Please login to view trade offers</h2>
            </div>
        );
    }

    return (
        <div className="container mx-auto py-8 px-4">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Trading</h1>
                <button
                    onClick={handleOpenCreateModal}
                    className="px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 font-semibold"
                >
                    + Create New Trade
                </button>
            </div>

            {/* Tabs */}
            <div className="flex mb-6 border-b">
                <button
                    className={`px-6 py-3 font-semibold ${ activeTab === 'received'
                            ? 'border-b-2 border-blue-500 text-blue-500'
                            : 'text-gray-500 hover:text-gray-700'
                    }`}
                    onClick={() => setActiveTab('received')}
                >
                    Received Offers ({receivedOffers.length})
                </button>
                <button
                    className={`px-6 py-3 font-semibold ${
                        activeTab === 'sent'
                            ? 'border-b-2 border-blue-500 text-blue-500'
                            : 'text-gray-500 hover:text-gray-700'
                    }`}
                    onClick={() => setActiveTab('sent')}
                >
                    Sent Offers ({sentOffers.length})
                </button>
            </div>

            {loading ? (
                <div className="text-center py-8">Loading...</div>
            ) : (
                <>
                    {/* Received Offers */}
                    {activeTab === 'received' && (
                        <div>
                            {receivedOffers.length === 0 ? (
                                <p className="text-gray-500 text-center py-8">No received trade offers</p>
                            ) : (
                                <div className="grid gap-4">
                                    {receivedOffers.map((offer, index) => (
                                        <div
                                            key={index}
                                            className="bg-white border rounded-lg shadow p-6"
                                        >
                                            <div className="flex justify-between items-start">
                                                <div className="flex-1">
                                                    <p className="font-semibold text-lg mb-2">
                                                        From: {offer.sender.username}
                                                    </p>
                                                    <div className="flex items-center gap-4 mb-2">
                                                        <div>
                                                            <p className="text-sm text-gray-600">They offer:</p>
                                                            <p className="font-medium">{offer.offeredSticker.name}</p>
                                                            <p className="text-sm italic">{offer.offeredSticker.latinName}</p>
                                                            {offer.sender.stickers && (
                                                                <p className="text-xs text-gray-500 mt-1">
                                                                    (they have: {offer.sender.stickers.find(s => s.sticker.id === offer.offeredSticker.id)?.quantity || 0})
                                                                </p>
                                                            )}
                                                        </div>
                                                        <span className="text-2xl">⇄</span>
                                                        <div>
                                                            <p className="text-sm text-gray-600">For your:</p>
                                                            <p className="font-medium">{offer.receivedSticker.name}</p>
                                                            <p className="text-sm italic">{offer.receivedSticker.latinName}</p>
                                                            {offer.receiver.stickers && (
                                                                <p className="text-xs text-gray-500 mt-1">
                                                                    (you have: {offer.receiver.stickers.find(s => s.sticker.id === offer.receivedSticker.id)?.quantity || 0})
                                                                </p>
                                                            )}
                                                        </div>
                                                    </div>
                                                    {offer.message && (
                                                        <p className="text-sm text-gray-600 italic mt-2">
                                                            Message: {offer.message}
                                                        </p>
                                                    )}
                                                    <p className="text-sm text-gray-500 mt-2">
                                                        Status: <span className="font-semibold">{offer.status}</span>
                                                    </p>
                                                </div>
                                                {offer.status === 'PENDING' && (
                                                    <div className="flex gap-2">
                                                        <button
                                                            onClick={() => handleAcceptOffer(offer)}
                                                            className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                                                        >
                                                            Accept
                                                        </button>
                                                        <button
                                                            onClick={() => handleRefuseOffer(offer)}
                                                            className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                                                        >
                                                            Refuse
                                                        </button>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}

                    {/* Sent Offers */}
                    {activeTab === 'sent' && (
                        <div>
                            {sentOffers.length === 0 ? (
                                <p className="text-gray-500 text-center py-8">No sent trade offers</p>
                            ) : (
                                <div className="grid gap-4">
                                    {sentOffers.map((offer, index) => (
                                        <div
                                            key={index}
                                            className="bg-white border rounded-lg shadow p-6"
                                        >
                                            <div className="flex justify-between items-start">
                                                <div className="flex-1">
                                                    <p className="font-semibold text-lg mb-2">
                                                        To: {offer.receiver.username}
                                                    </p>
                                                    <div className="flex items-center gap-4 mb-2">
                                                        <div>
                                                            <p className="text-sm text-gray-600">You offered:</p>
                                                            <p className="font-medium">{offer.offeredSticker.name}</p>
                                                            <p className="text-sm italic">{offer.offeredSticker.latinName}</p>
                                                            {offer.sender.stickers && (
                                                                <p className="text-xs text-gray-500 mt-1">
                                                                    (you have: {offer.sender.stickers.find(s => s.sticker.id === offer.offeredSticker.id)?.quantity || 0})
                                                                </p>
                                                            )}
                                                        </div>
                                                        <span className="text-2xl">⇄</span>
                                                        <div>
                                                            <p className="text-sm text-gray-600">For their:</p>
                                                            <p className="font-medium">{offer.receivedSticker.name}</p>
                                                            <p className="text-sm italic">{offer.receivedSticker.latinName}</p>
                                                            {offer.receiver.stickers && (
                                                                <p className="text-xs text-gray-500 mt-1">
                                                                    (they have: {offer.receiver.stickers.find(s => s.sticker.id === offer.receivedSticker.id)?.quantity || 0})
                                                                </p>
                                                            )}
                                                        </div>
                                                    </div>
                                                    {offer.message && (
                                                        <p className="text-sm text-gray-600 italic mt-2">
                                                            Message: {offer.message}
                                                        </p>
                                                    )}
                                                    <p className="text-sm text-gray-500 mt-2">
                                                        Status: <span className="font-semibold">{offer.status}</span>
                                                    </p>
                                                </div>
                                                {offer.status === 'PENDING' && (
                                                    <div className="flex gap-2">
                                                        <button
                                                            onClick={() => handleCancelOffer(offer)}
                                                            className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                                                        >
                                                            Cancel
                                                        </button>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </>
            )}

            {/* Create Trade Modal */}
            {showCreateModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="p-6">
                            <div className="flex justify-between items-center mb-6">
                                <h2 className="text-2xl font-bold">Create New Trade</h2>
                                <button
                                    onClick={() => {
                                        setShowCreateModal(false);
                                        resetCreateForm();
                                    }}
                                    className="text-gray-500 hover:text-gray-700 text-2xl"
                                >
                                    ×
                                </button>
                            </div>

                            <div className="space-y-6">
                                {/* Select Receiver */}
                                <div>
                                    <label className="block text-sm font-semibold mb-2">
                                        Trade with: <span className="text-red-500">*</span>
                                    </label>
                                    <select
                                        value={selectedReceiver || ''}
                                        onChange={(e) => handleReceiverChange(Number(e.target.value))}
                                        className="w-full border rounded-lg px-3 py-2"
                                        disabled={loading}
                                    >
                                        <option value="">Select a collector...</option>
                                        {collectors.map((collector) => (
                                            <option key={collector.id} value={collector.id}>
                                                {collector.username}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {/* Select Sticker to Offer */}
                                <div>
                                    <label className="block text-sm font-semibold mb-2">
                                        You offer: <span className="text-red-500">*</span>
                                    </label>
                                    <select
                                        value={selectedOfferedSticker || ''}
                                        onChange={(e) => setSelectedOfferedSticker(Number(e.target.value))}
                                        className="w-full border rounded-lg px-3 py-2"
                                        disabled={loading}
                                    >
                                        <option value="">Select a sticker you own...</option>
                                        {myStickers
                                            .filter(sw => sw.quantity > 0)
                                            .map((stickerWithQty) => (
                                                <option key={stickerWithQty.sticker.id} value={stickerWithQty.sticker.id}>
                                                    {stickerWithQty.sticker.name} ({stickerWithQty.sticker.latinName}) - Qty: {stickerWithQty.quantity}
                                                </option>
                                            ))
                                        }
                                    </select>
                                </div>

                                {/* Select Sticker to Request */}
                                {selectedReceiver && (
                                    <div>
                                        <label className="block text-sm font-semibold mb-2">
                                            You want: <span className="text-red-500">*</span>
                                        </label>
                                        <select
                                            value={selectedRequestedSticker || ''}
                                            onChange={(e) => setSelectedRequestedSticker(Number(e.target.value))}
                                            className="w-full border rounded-lg px-3 py-2"
                                            disabled={loading || receiverStickers.length === 0}
                                        >
                                            <option value="">Select a sticker they own...</option>
                                            {receiverStickers
                                                .filter(sw => sw.quantity > 0)
                                                .map((stickerWithQty) => (
                                                    <option key={stickerWithQty.sticker.id} value={stickerWithQty.sticker.id}>
                                                        {stickerWithQty.sticker.name} ({stickerWithQty.sticker.latinName}) - Qty: {stickerWithQty.quantity}
                                                    </option>
                                                ))
                                            }
                                        </select>
                                    </div>
                                )}

                                {/* Message */}
                                <div>
                                    <label className="block text-sm font-semibold mb-2">
                                        Message (optional)
                                    </label>
                                    <textarea
                                        value={tradeMessage}
                                        onChange={(e) => setTradeMessage(e.target.value)}
                                        className="w-full border rounded-lg px-3 py-2"
                                        rows={3}
                                        placeholder="Add a message to your trade offer..."
                                        disabled={loading}
                                    />
                                </div>

                                {/* Actions */}
                                <div className="flex gap-3 justify-end">
                                    <button
                                        onClick={() => {
                                            setShowCreateModal(false);
                                            resetCreateForm();
                                        }}
                                        className="px-6 py-2 border rounded-lg hover:bg-gray-50"
                                        disabled={loading}
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        onClick={handleCreateTrade}
                                        className="px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed"
                                        disabled={loading || !selectedReceiver || !selectedOfferedSticker || !selectedRequestedSticker}
                                    >
                                        {loading ? 'Sending...' : 'Send Trade Offer'}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Trading;