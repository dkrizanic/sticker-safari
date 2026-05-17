import React, { useState, useEffect } from 'react';
import apiService, { TradeOfferResponse } from '../api/apiService';
import { useUser } from '../context/UserContext';
import { useToast } from '../context/ToastContext';

const Trading = (): JSX.Element => {
    const { currentUser } = useUser();
    const { addToast } = useToast();
    const [receivedOffers, setReceivedOffers] = useState<TradeOfferResponse[]>([]);
    const [sentOffers, setSentOffers] = useState<TradeOfferResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState<'received' | 'sent'>('received');

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

    if (!currentUser) {
        return (
            <div className="py-8 text-center">
                <h2 className="text-2xl font-bold">Please login to view trade offers</h2>
            </div>
        );
    }

    return (
        <div className="container mx-auto py-8 px-4">
            <h1 className="text-3xl font-bold mb-6">Trading</h1>

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
                                                        </div>
                                                        <span className="text-2xl">⇄</span>
                                                        <div>
                                                            <p className="text-sm text-gray-600">For your:</p>
                                                            <p className="font-medium">{offer.receivedSticker.name}</p>
                                                            <p className="text-sm italic">{offer.receivedSticker.latinName}</p>
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
                                                        </div>
                                                        <span className="text-2xl">⇄</span>
                                                        <div>
                                                            <p className="text-sm text-gray-600">For their:</p>
                                                            <p className="font-medium">{offer.receivedSticker.name}</p>
                                                            <p className="text-sm italic">{offer.receivedSticker.latinName}</p>
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
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default Trading;