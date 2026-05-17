import React, { useState, useEffect } from 'react';
import AlbumCard from './AlbumCard';
import apiService from '../api/apiService';
import { useUser } from '../context/UserContext';
import { useNavigate } from 'react-router-dom';
import { Album } from '../types';


const Collection = (): JSX.Element => {

    const [albums, setAlbums] = useState<Album[]>([]);
    const [ownedStickers, setOwnedStickers] = useState<number[]>([]);
    const {currentUser} = useUser();
    const navigate = useNavigate();

    // All hooks must be called before any conditional returns
    useEffect(() => {
        const fetchAlbums = async () => {
            try {
                // Fetch all albums and filter only PUBLISHED ones
                const allAlbums = await apiService.getAllAlbums();
                const publishedAlbums = allAlbums.filter(album => album.status === 'PUBLISHED');
                setAlbums(publishedAlbums);
            } catch (error) {
                console.error('Failed to fetch albums:', error);
            }
        };
        
        fetchAlbums();
    }, []);

    useEffect(() => {
        const fetchOwnedStickers = async () => {
            if (currentUser?.id) {
                try {
                    const stickers = await apiService.getCollectorStickers(currentUser.id);
                    setOwnedStickers(stickers);
                } catch (error) {
                    console.error('Failed to fetch owned stickers:', error);
                }
            }
        };
        
        fetchOwnedStickers();
    }, [currentUser]);

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

    return (
        <div className="py-4 ml-2">
            {albums.map((album) => (
                <AlbumCard 
                    key={album.id}
                    album={album}
                    ownedStickers={ownedStickers}/>
            ))}
        </div>
    );
};
export default Collection;