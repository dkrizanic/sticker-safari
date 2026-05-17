import React, { useState, useEffect } from 'react';
import apiService from '../api/apiService';
import { Sticker, Album } from '../types';
import { useUser } from '../context/UserContext';
import { useNavigate } from 'react-router-dom';
import { useToast } from '../context/ToastContext';


const PackOpening = (): JSX.Element => {
    const [stickerNumber, setStickerNumber] = useState<string>('');
    const [selectedAlbum, setSelectedAlbum] = useState<string>('1');
    const [packResults, setPackResults] = useState<Sticker[]>([]);
    const [albums, setAlbums] = useState<Album[]>([]);
    const [loading, setLoading] = useState(true);
    const { currentUser } = useUser();
    const navigate = useNavigate();
    const { addToast } = useToast();

    const [hasResults, setHasResults] = useState(false);
    
    // All hooks must be called before any conditional returns
    useEffect(() => {
        setHasResults(packResults.length > 0);
    }, [packResults]);

    // Fetch albums from backend
    useEffect(() => {
        const fetchAlbums = async () => {
            try {
                const data = await apiService.getAllAlbums();
                setAlbums(data);
                if (data.length > 0) {
                    setSelectedAlbum(data[0].id.toString());
                }
            } catch (error) {
                console.error('Failed to fetch albums:', error);
                addToast('Failed to load albums', 'error');
            } finally {
                setLoading(false);
            }
        };
        fetchAlbums();
    }, [addToast]);

    // Redirect if admin tries to access
    useEffect(() => {
        if (currentUser?.role === 'ADMIN') {
            navigate('/admin');
        }
    }, [currentUser, navigate]);

    // Don't render if admin
    if (currentUser?.role === 'ADMIN') {
        return <></>;
    }

    const handleOpenPack = async () => {
        if (!currentUser?.id) {
            addToast('Please login first to open packs', 'error');
            return;
        }

        const numStickers = parseInt(stickerNumber, 10);
        if (isNaN(numStickers) || numStickers < 1) {
            addToast('Please enter a valid number of stickers', 'error');
            return;
        }

        // Clear previous results before opening new pack
        setPackResults([]);

        try {
            const data = await apiService.openPack(parseInt(selectedAlbum, 10), currentUser.id, numStickers);
            setPackResults(data);
            addToast(`Successfully opened ${data.length} sticker(s)!`, 'success');
        } catch (error: any) {
            console.error('Failed to open pack:', error);
            const errorMessage = error.response?.data?.message || 'Failed to open pack. Please try again.';
            addToast(errorMessage, 'error');
        }
    };

    const handleClearResults = () => {
        setPackResults([]);
        setStickerNumber('');
    };

    return (
        <div className="container mx-auto mt-10 text-center">
            <h1 className="text-3xl font-bold mb-6">Pack Opening</h1>
            {loading ? (
                <p>Loading albums...</p>
            ) : (
                <>
                    <div className="flex justify-center items-center mb-4">
                        <label className="mr-2">Number of Stickers:</label>
                        <input
                            type="number"
                            min="1"
                            className="border p-2"
                            value={stickerNumber}
                            onChange={(e) => setStickerNumber(e.target.value)}
                        />
                    </div>
                    <div className="flex justify-center items-center mb-4">
                        <label className="mr-2">Select Album:</label>
                        <select
                            className="border p-2"
                            value={selectedAlbum}
                            onChange={(e) => setSelectedAlbum(e.target.value)}
                        >
                            {albums.map((album) => (
                                <option key={album.id} value={album.id}>
                                    {album.name}
                                </option>
                            ))}
                        </select>
                    </div>
                    <button
                        className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-700"
                        onClick={handleOpenPack}
                    >
                        Open Pack
                    </button>
                    {hasResults && (
                        <button
                            className="ml-4 bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700"
                            onClick={handleClearResults}
                        >
                            Clear Results
                        </button>
                    )}
                    <div className="mt-4">
                        {hasResults && (
                            <>
                                <h2 className="text-2xl font-bold mb-4">Your New Stickers!</h2>
                                <div className="flex flex-wrap justify-center">
                                    {packResults.map((sticker, index) => (
                                        <img
                                            key={`${sticker.id}-${index}`}
                                            src={sticker.src}
                                            alt={`Sticker ${sticker.id}`}
                                            className="w-40 h-30 m-2 border border-black p-1"
                                        />
                                    ))}
                                </div>
                            </>
                        )}
                    </div>
                </>
            )}
        </div>
    );

};
export default PackOpening;