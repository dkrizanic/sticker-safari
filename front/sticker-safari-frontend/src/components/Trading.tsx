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

    // Global trade modal state
    const [showGlobalModal, setShowGlobalModal] = useState(false);
    const [allStickers, setAllStickers] = useState<any[]>([]);
    const [globalOfferedSticker, setGlobalOfferedSticker] = useState<number | null>(null);
    const [globalRequestedSticker, setGlobalRequestedSticker] = useState<number | null>(null);

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

    const handleOpenGlobalModal = async () => {
        setShowGlobalModal(true);
        setLoading(true);
        try {
            // Load current user's stickers
            if (currentUser?.id) {
                const userCollector = await apiService.getCollector(currentUser.id);
                setMyStickers(userCollector.stickers || []);
            }
            
            // Load all stickers in the system
            const stickers = await apiService.getAllStickers();
            setAllStickers(stickers);
        } catch (error) {
            console.error('Failed to load stickers:', error);
            addToast('Failed to load stickers', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleCreateGlobalTrade = async () => {
        if (!currentUser?.id || !globalOfferedSticker || !globalRequestedSticker) {
            addToast('Please select both stickers', 'error');
            return;
        }

        setLoading(true);
        try {
            const result = await apiService.sendGlobalTradeOffer({
                senderId: currentUser.id,
                offeredStickerId: globalOfferedSticker,
                receivedStickerId: globalRequestedSticker
            });
            addToast(result || 'Global trade offer sent successfully! 🌍', 'success');
            setShowGlobalModal(false);
            resetGlobalForm();
            fetchTradeOffers();
        } catch (error: any) {
            console.error('Failed to create global trade offer:', error);
            const errorMessage = error.response?.data?.message || error.response?.data || 'Failed to create global trade offer';
            addToast(errorMessage, 'error');
        } finally {
            setLoading(false);
        }
    };

    const resetGlobalForm = () => {
        setGlobalOfferedSticker(null);
        setGlobalRequestedSticker(null);
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
                <div className="flex gap-3">
                    <button
                        onClick={handleOpenGlobalModal}
                        className="px-6 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 font-semibold"
                    >
                        🌍 Global Trade
                    </button>
                    <button
                        onClick={handleOpenCreateModal}
                        className="px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 font-semibold"
                    >
                        + Specific Trade
                    </button>
                </div>
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
                                <div className="flex flex-col items-center gap-4">
                                    {receivedOffers.map((offer, index) => (
                                        <div
                                            key={index}
                                            className="bg-gradient-to-br from-white to-slate-50 border-2 border-slate-200 rounded-xl shadow-md hover:shadow-lg transition-all p-4 w-full max-w-3xl"
                                        >
                                            <div className="flex justify-between items-center gap-4">
                                                <div className="flex-1 min-w-0">
                                                    <div className="flex items-center gap-2 mb-3">
                                                        <span className="text-lg">👤</span>
                                                        <span className="font-bold text-slate-800">From: {offer.sender.username}</span>
                                                        <span className={`ml-auto text-xs px-2 py-1 rounded-full font-semibold ${
                                                            offer.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                                                            offer.status === 'ACCEPTED' ? 'bg-green-100 text-green-700' :
                                                            'bg-gray-100 text-gray-700'
                                                        }`}>
                                                            {offer.status}
                                                        </span>
                                                    </div>
                                                    
                                                    <div className="flex items-center gap-3">
                                                        <div className="bg-green-50 border border-green-200 rounded-lg p-2 flex-1">
                                                            <p className="text-xs text-green-600 font-semibold mb-0.5">They offer:</p>
                                                            <p className="font-bold text-sm text-slate-800 truncate">{offer.offeredSticker.name}</p>
                                                            <p className="text-xs text-slate-500 italic truncate">{offer.offeredSticker.latinName}</p>
                                                            {offer.sender.stickers && (
                                                                <p className="text-xs text-green-600 mt-0.5">
                                                                    (they have: {offer.sender.stickers.find(s => s.sticker.id === offer.offeredSticker.id)?.quantity || 0})
                                                                </p>
                                                            )}
                                                        </div>
                                                        
                                                        <div className="flex-shrink-0">
                                                            <span className="text-2xl">⇄</span>
                                                        </div>
                                                        
                                                        <div className="bg-blue-50 border border-blue-200 rounded-lg p-2 flex-1">
                                                            <p className="text-xs text-blue-600 font-semibold mb-0.5">For your:</p>
                                                            <p className="font-bold text-sm text-slate-800 truncate">{offer.receivedSticker.name}</p>
                                                            <p className="text-xs text-slate-500 italic truncate">{offer.receivedSticker.latinName}</p>
                                                            {offer.receiver.stickers && (
                                                                <p className="text-xs text-blue-600 mt-0.5">
                                                                    (you have: {offer.receiver.stickers.find(s => s.sticker.id === offer.receivedSticker.id)?.quantity || 0})
                                                                </p>
                                                            )}
                                                        </div>
                                                    </div>
                                                    
                                                    {offer.message && (
                                                        <div className="mt-2 text-xs text-slate-600 italic bg-slate-100 rounded px-2 py-1">
                                                            💬 {offer.message}
                                                        </div>
                                                    )}
                                                </div>
                                                
                                                {offer.status === 'PENDING' && (
                                                    <div className="flex gap-2 flex-shrink-0">
                                                        <button
                                                            onClick={() => handleAcceptOffer(offer)}
                                                            className="px-4 py-2 bg-green-500 text-white text-sm font-semibold rounded-lg hover:bg-green-600 transition-colors shadow-md"
                                                        >
                                                            Accept
                                                        </button>
                                                        <button
                                                            onClick={() => handleRefuseOffer(offer)}
                                                            className="px-4 py-2 bg-red-500 text-white text-sm font-semibold rounded-lg hover:bg-red-600 transition-colors shadow-md"
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
                                <div className="flex flex-col items-center gap-4">
                                    {sentOffers.map((offer, index) => (
                                        <div
                                            key={index}
                                            className="bg-gradient-to-br from-white to-slate-50 border-2 border-slate-200 rounded-xl shadow-md hover:shadow-lg transition-all p-4 w-full max-w-3xl"
                                        >
                                            <div className="flex justify-between items-center gap-4">
                                                <div className="flex-1 min-w-0">
                                                    <div className="flex items-center gap-2 mb-3">
                                                        <span className="text-lg">👤</span>
                                                        <span className="font-bold text-slate-800">To: {offer.receiver.username}</span>
                                                        <span className={`ml-auto text-xs px-2 py-1 rounded-full font-semibold ${
                                                            offer.status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                                                            offer.status === 'ACCEPTED' ? 'bg-green-100 text-green-700' :
                                                            'bg-gray-100 text-gray-700'
                                                        }`}>
                                                            {offer.status}
                                                        </span>
                                                    </div>
                                                    
                                                    <div className="flex items-center gap-3">
                                                        <div className="bg-blue-50 border border-blue-200 rounded-lg p-2 flex-1">
                                                            <p className="text-xs text-blue-600 font-semibold mb-0.5">You offered:</p>
                                                            <p className="font-bold text-sm text-slate-800 truncate">{offer.offeredSticker.name}</p>
                                                            <p className="text-xs text-slate-500 italic truncate">{offer.offeredSticker.latinName}</p>
                                                            {offer.sender.stickers && (
                                                                <p className="text-xs text-blue-600 mt-0.5">
                                                                    (you have: {offer.sender.stickers.find(s => s.sticker.id === offer.offeredSticker.id)?.quantity || 0})
                                                                </p>
                                                            )}
                                                        </div>
                                                        
                                                        <div className="flex-shrink-0">
                                                            <span className="text-2xl">⇄</span>
                                                        </div>
                                                        
                                                        <div className="bg-green-50 border border-green-200 rounded-lg p-2 flex-1">
                                                            <p className="text-xs text-green-600 font-semibold mb-0.5">For their:</p>
                                                            <p className="font-bold text-sm text-slate-800 truncate">{offer.receivedSticker.name}</p>
                                                            <p className="text-xs text-slate-500 italic truncate">{offer.receivedSticker.latinName}</p>
                                                            {offer.receiver.stickers && (
                                                                <p className="text-xs text-green-600 mt-0.5">
                                                                    (they have: {offer.receiver.stickers.find(s => s.sticker.id === offer.receivedSticker.id)?.quantity || 0})
                                                                </p>
                                                            )}
                                                        </div>
                                                    </div>
                                                    
                                                    {offer.message && (
                                                        <div className="mt-2 text-xs text-slate-600 italic bg-slate-100 rounded px-2 py-1">
                                                            💬 {offer.message}
                                                        </div>
                                                    )}
                                                </div>
                                                
                                                {offer.status === 'PENDING' && (
                                                    <div className="flex gap-2 flex-shrink-0">
                                                        <button
                                                            onClick={() => handleCancelOffer(offer)}
                                                            className="px-4 py-2 bg-red-500 text-white text-sm font-semibold rounded-lg hover:bg-red-600 transition-colors shadow-md"
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

            {/* Global Trade Modal */}
            {showGlobalModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="p-6">
                            <div className="flex justify-between items-center mb-6">
                                <div>
                                    <h2 className="text-2xl font-bold">🌍 Global Trade</h2>
                                    <p className="text-sm text-gray-600 mt-1">
                                        Send offers to all collectors who have the sticker you want and need the one you offer
                                    </p>
                                </div>
                                <button
                                    onClick={() => {
                                        setShowGlobalModal(false);
                                        resetGlobalForm();
                                    }}
                                    className="text-gray-500 hover:text-gray-700 text-2xl"
                                >
                                    ×
                                </button>
                            </div>

                            <div className="space-y-6">
                                {/* Select Sticker to Offer */}
                                <div>
                                    <label className="block text-sm font-semibold mb-2">
                                        You offer: <span className="text-red-500">*</span>
                                    </label>
                                    <select
                                        value={globalOfferedSticker || ''}
                                        onChange={(e) => setGlobalOfferedSticker(Number(e.target.value))}
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
                                    <p className="text-xs text-gray-500 mt-1">
                                        This sticker will be offered to collectors who need it
                                    </p>
                                </div>

                                {/* Select Sticker to Request */}
                                <div>
                                    <label className="block text-sm font-semibold mb-2">
                                        You want: <span className="text-red-500">*</span>
                                    </label>
                                    <select
                                        value={globalRequestedSticker || ''}
                                        onChange={(e) => setGlobalRequestedSticker(Number(e.target.value))}
                                        className="w-full border rounded-lg px-3 py-2"
                                        disabled={loading || allStickers.length === 0}
                                    >
                                        <option value="">Select any sticker...</option>
                                        {allStickers.map((sticker) => (
                                            <option key={sticker.id} value={sticker.id}>
                                                {sticker.name} ({sticker.latinName})
                                            </option>
                                        ))}
                                    </select>
                                    <p className="text-xs text-gray-500 mt-1">
                                        Collectors who have duplicates of this sticker and need yours will receive an offer
                                    </p>
                                </div>

                                {/* Info Box */}
                                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                                    <div className="flex">
                                        <div className="flex-shrink-0">
                                            <svg className="h-5 w-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                                            </svg>
                                        </div>
                                        <div className="ml-3">
                                            <p className="text-sm text-blue-700">
                                                <strong>How it works:</strong> Your trade offer will be automatically sent to all collectors who:
                                            </p>
                                            <ul className="list-disc list-inside text-sm text-blue-700 mt-1">
                                                <li>Have extra copies of the sticker you want</li>
                                                <li>Don't have the sticker you're offering</li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>

                                {/* Actions */}
                                <div className="flex gap-3 justify-end">
                                    <button
                                        onClick={() => {
                                            setShowGlobalModal(false);
                                            resetGlobalForm();
                                        }}
                                        className="px-6 py-2 border rounded-lg hover:bg-gray-50"
                                        disabled={loading}
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        onClick={handleCreateGlobalTrade}
                                        className="px-6 py-2 bg-purple-500 text-white rounded-lg hover:bg-purple-600 disabled:opacity-50 disabled:cursor-not-allowed"
                                        disabled={loading || !globalOfferedSticker || !globalRequestedSticker}
                                    >
                                        {loading ? 'Sending...' : 'Send Global Offer 🌍'}
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