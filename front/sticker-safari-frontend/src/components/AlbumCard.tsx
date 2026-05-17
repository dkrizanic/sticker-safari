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
        
        // Immediate check to prevent double-clicks
        if (isCompleting) {
            return;
        }
        
        if (!currentUser?.id) {
            addToast('Please log in to complete albums', 'error');
            return;
        }

        if (!canComplete) {
            addToast('You need to collect all stickers first!', 'warning');
            return;
        }

        setIsCompleting(true);
        setCanComplete(false); // Immediately disable to prevent race condition
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
            // Re-enable if there was an error
            setIsCompleting(false);
            setCanComplete(true);
        }
    };
    
    return (
        <div className="py-3">
            <div
                className="relative overflow-hidden bg-gradient-to-br from-slate-50 to-slate-100 dark:from-gray-800 dark:to-gray-900 border-2 border-slate-200 dark:border-gray-700 rounded-xl shadow-lg hover:shadow-2xl transition-all duration-300 cursor-pointer"
                onClick={handleArrowClick}>
                {/* Decorative background pattern */}
                <div className="absolute inset-0 opacity-5">
                    <div className="absolute inset-0" style={{
                        backgroundImage: 'radial-gradient(circle, #000 1px, transparent 1px)',
                        backgroundSize: '20px 20px'
                    }}></div>
                </div>
                
                <div className="relative p-6">
                    <div className="flex flex-row justify-between items-center">
                        <div className="flex-1">
                            <h3 className="text-2xl font-bold mb-3 bg-gradient-to-r from-slate-700 to-slate-900 dark:from-slate-200 dark:to-white bg-clip-text text-transparent">
                                {album.name}
                            </h3>
                            
                            {/* Progress Bar */}
                            <div className="mb-2">
                                <div className="flex items-center justify-between mb-1">
                                    <span className="text-sm font-medium text-slate-600 dark:text-slate-300">
                                        {completion.toFixed(1)}% complete
                                    </span>
                                    <span className="text-xs text-slate-500 dark:text-slate-400">
                                        {album.stickers.filter(s => ownedStickers.includes(s.id)).length} / {album.stickers.length} stickers
                                    </span>
                                </div>
                                <div className="w-full h-3 bg-slate-200 dark:bg-gray-700 rounded-full overflow-hidden shadow-inner">
                                    <div 
                                        className="h-full bg-gradient-to-r from-blue-400 via-blue-500 to-blue-600 dark:from-blue-500 dark:via-blue-600 dark:to-blue-700 transition-all duration-500 ease-out rounded-full shadow-md"
                                        style={{ width: `${completion}%` }}
                                    >
                                        {completion > 10 && (
                                            <div className="h-full flex items-center justify-end pr-2">
                                                <span className="text-xs font-bold text-white drop-shadow">★</span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                            
                            {canComplete && (
                                <button
                                    onClick={handleCompleteAlbum}
                                    disabled={isCompleting}
                                    className={`mt-3 px-4 py-2 bg-gradient-to-r from-green-500 to-green-600 text-white text-sm font-semibold rounded-lg hover:from-green-600 hover:to-green-700 disabled:from-gray-400 disabled:to-gray-500 transition-all duration-200 shadow-md hover:shadow-lg transform hover:scale-105 ${
                                        isCompleting ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'
                                    }`}
                                >
                                    {isCompleting ? '⏳ Completing...' : '✓ Complete Album & Earn Star'}
                                </button>
                            )}
                        </div>
                        <div className="ml-4">
                            {isHidden ?
                                <ArrowDown className='w-6 h-6 text-slate-600 dark:text-slate-300 transition-transform duration-200'/> :
                                <ArrowUp className='w-6 h-6 text-slate-600 dark:text-slate-300 transition-transform duration-200'/>}
                        </div>
                    </div>
                </div>
            </div>
            {!isHidden && (
                <div className="mt-4 p-4 bg-gradient-to-b from-slate-50 to-white dark:from-gray-900 dark:to-gray-800 rounded-xl border-2 border-slate-200 dark:border-gray-700 shadow-inner">
                    <AlbumStickers albumStickers = {album.stickers} ownedStickers={ownedStickers}/>
                </div>
            )}
        </div>
    )
}

export default AlbumCard;