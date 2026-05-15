import React, { useState, useEffect } from 'react';
import apiService from '../api/apiService';
import { Sticker } from '../types';
import { useUser } from '../context/UserContext';


const PackOpening = (): JSX.Element => {
    const [stickerNumber, setStickerNumber] = useState<string>('');
    const [selectedAlbum, setSelectedAlbum] = useState<string>('1');
    const [packResults, setPackResults] = useState<Sticker[]>([]);
    const { currentUser } = useUser();

    const [hasResults, setHasResults] = useState(false);
    
    useEffect(() => {
        setHasResults(packResults.length > 0);
    }, [packResults]);

    const handleOpenPack = async () => {
        if (!currentUser?.id) {
            alert('Please login first to open packs');
            return;
        }

        const numStickers = parseInt(stickerNumber, 10);
        if (isNaN(numStickers) || numStickers < 1) {
            alert('Please enter a valid number of stickers');
            return;
        }

        try {
            const data = await apiService.openPack(parseInt(selectedAlbum, 10), currentUser.id, numStickers);
            setPackResults(data);
        } catch (error) {
            console.error('Failed to open pack:', error);
            alert('Failed to open pack. Make sure the backend is running.');
        }
    };

    return (
        <div className="container mx-auto mt-10 text-center">
            <h1 className="text-3xl font-bold mb-6">Pack Opening</h1>
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
                    <option value="1">Zivotinjsko carstvo</option>
                    <option value="2">Zvjerici</option>
                </select>
            </div>
            <button
                className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-700"
                onClick={handleOpenPack}
            >
                Open Pack
            </button>
            <div className="mt-4">
                {hasResults && (
                    <div className="flex flex-wrap justify-center">
                        {packResults.map((sticker) => (
                            <img
                                key={sticker.id}
                                src={sticker.src}
                                alt={`Sticker ${sticker.id}`}
                                className="w-40 h-30 m-2 border border-black p-1"
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );

};
export default PackOpening;