import React, { useState, useEffect } from 'react';
import AlbumCard from './AlbumCard';
import apiService from '../api/apiService';
import { useUser } from '../context/UserContext';
import { Album } from '../types';


const Collection = (): JSX.Element => {

    const [albums, setAlbums] = useState<Album[]>([]);
    const {currentUser} = useUser();

    //mock
    const generateRandomOwnedStickers = (): number[] => { 
        const ownedStickers: number[] = [];

        albums.forEach((album) => {
            album.stickers.forEach((sticker) => {
                const shouldAddSticker = Math.random() < 0.5;

                if (shouldAddSticker) {
                    ownedStickers.push(sticker.id);
                }
            });
        });

        return ownedStickers;
    }

    useEffect(() => {
        const fetchAlbums = async () => {
            try {
                // Fetch both albums (IDs 1 and 2 as per database)
                const [album1, album2] = await Promise.all([
                    apiService.getAlbum(1),
                    apiService.getAlbum(2)
                ]);
                setAlbums([album1, album2]);
            } catch (error) {
                console.error('Failed to fetch albums:', error);
            }
        };
        
        fetchAlbums();
    }, []);

    return (
        <div className="py-4 ml-2">
            {albums.map((album) => (
                <AlbumCard 
                    key={album.id}
                    album={album}
                    ownedStickers={currentUser ? generateRandomOwnedStickers() : []}/>
            ))}
        </div>
    );
};
export default Collection;