import React, { useState, useEffect } from 'react';
import AlbumStickers from './AlbumStickers';
import { ReactComponent as ArrowUp } from "./assets/svg/up-arrow.svg";
import { ReactComponent as ArrowDown } from "./assets/svg/down-arrow.svg";
import { Album } from '../types';
import apiService from '../api/apiService';
import { useUser } from '../context/UserContext';
import { useToast } from '../context/ToastContext';

interface AlbumCardProps {
  album: Album;
  ownedStickers: number[];
  onAlbumCompleted?: () => void;
}

function AlbumCard({ album, ownedStickers, onAlbumCompleted }: AlbumCardProps): JSX.Element {
    const [isHidden, setIsHidden] = useState<boolean>(true);
    const [completion, setCompletion] = useState<number>(0);
    const [canComplete, setCanComplete] = useState<boolean>(false);
    const [isCompleting, setIsCompleting] = useState<boolean>(false);
    const { currentUser } = useUser();
    const { addToast } = useToast();

    useEffect(() => {
        const checkCompletion = async () => {
            if (!currentUser?.id) return;
            
            try {
                const completionData = await apiService.getAlbumCompletion(album.id, currentUser.id);
                setCompletion(completionData.completion);
                setCanComplete(completionData.completion === 100);
            } catch (error) {
                console.error('Failed to check album completion:', error);
            }
        };

        checkCompletion();
    }, [album.id, currentUser, ownedStickers]);

    const handleArrowClick = () => {
        setIsHidden(!isHidden);
    };

    const handleCompleteAlbum = async (e: React.MouseEvent) => {
        e.stopPropagation(); // Prevent card collapse
        
        if (!currentUser?.id) {
            addToast('Please log in to complete albums', 'error');
            return;
        }

        if (!canComplete) {
            addToast('You need to collect all stickers first!', 'warning');
            return;
        }

        setIsCompleting(true);
        try {
            await apiService.completeAlbum(album.id, currentUser.id);
            addToast(`Congratulations! You completed "${album.name}" and earned a star! ⭐`, 'success');
            
            // Refresh the page or call callback to update user data
            if (onAlbumCompleted) {
                onAlbumCompleted();
            } else {
                window.location.reload();
            }
        } catch (error: any) {
            console.error('Failed to complete album:', error);
            addToast(
                error.response?.data?.message || 'Failed to complete album',
                'error'
            );
        } finally {
            setIsCompleting(false);
        }
    };
    
    return (
        <div className="py-2">
            <div
                className="flex flex-row justify-between items-center block max-w-sm p-6 bg-white border border-gray-200 rounded-lg shadow hover:bg-gray-100 dark:bg-gray-800 dark:border-gray-700 dark:hover:bg-gray-700 cursor-pointer"
                onClick={handleArrowClick}>
                <div className="flex-1">
                    <h3 className="text-lg font-semibold mb-1">{album.name}</h3>
                    <div className="flex items-center gap-2">
                        <div className="text-sm text-gray-600 dark:text-gray-400">
                            {completion.toFixed(1)}% complete
                        </div>
                        {canComplete && (
                            <button
                                onClick={handleCompleteAlbum}
                                disabled={isCompleting}
                                className="ml-2 px-3 py-1 bg-green-500 text-white text-sm rounded hover:bg-green-600 disabled:bg-gray-400 transition-colors"
                            >
                                {isCompleting ? 'Completing...' : '✓ Complete Album'}
                            </button>
                        )}
                    </div>
                </div>
                {isHidden ?
                    <ArrowDown className='w-5 h-5 ml-2'/> :
                    <ArrowUp className='w-5 h-5 ml-2'/>}
            </div>
            {!isHidden && (
                <AlbumStickers albumStickers = {album.stickers} ownedStickers={ownedStickers}/>
            )}
        </div>
    )
}

export default AlbumCard;