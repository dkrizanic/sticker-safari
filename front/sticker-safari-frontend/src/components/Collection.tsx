import React, { useState, useEffect } from 'react';
import AlbumCard from './AlbumCard';
import mockApi from '../api/mockApi';
import { useUser } from '../context/UserContext';
import { Album } from '../types';


const Collection = (): JSX.Element => {

    const [albums, setAlbums] = useState<Album[]>([]);
    const {currentUser} = useUser();

    //mock
    const generateRandomOwnedStickers = (user: any): number[] => { 
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
            const data = await mockApi.getAlbums() as any; 
            setAlbums(data as string[]);
        };
        
        fetchAlbums();
    }, []);

    return (
        <div className="py-4 ml-2">
            {albums.map((album) => (
                <AlbumCard album={album}
                           ownedStickers={currentUser ? generateRandomOwnedStickers() : []}/>
            ))}
        </div>
    );
};
export default Collection;