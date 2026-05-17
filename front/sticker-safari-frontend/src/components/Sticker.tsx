import React, { useState } from 'react';
import { Sticker as StickerType } from '../types';

interface StickerProps {
  sticker: StickerType;
  hidden: boolean;
}

function Sticker({ sticker, hidden }: StickerProps): JSX.Element {
    const [imageError, setImageError] = useState(false);
    const [imageLoaded, setImageLoaded] = useState(false);

    const handleImageError = () => {
        setImageError(true);
        setImageLoaded(true);
    };

    const handleImageLoad = () => {
        setImageLoaded(true);
    };

    // Add size parameters to Unsplash URLs for better performance
    const optimizedSrc = sticker.src.includes('unsplash.com') 
        ? `${sticker.src}?w=400&h=300&fit=crop&q=80`
        : sticker.src;

    return (
        <div className="group perspective-1000">
            <div
                className="relative h-64 w-full transition-all duration-500 [transform-style:preserve-3d] group-hover:[transform:rotateY(180deg)]">
                {/* Front side */}
                <div className="absolute inset-0 [backface-visibility:hidden]">
                    {!hidden ? (
                        <div className="h-full w-full rounded-lg overflow-hidden shadow-xl border-4 border-white ring-2 ring-slate-300 dark:ring-slate-600 bg-white transform transition-all duration-300 group-hover:shadow-2xl group-hover:scale-105">
                            {!imageLoaded && (
                                <div className="h-full w-full flex items-center justify-center bg-gradient-to-br from-slate-200 to-slate-300">
                                    <div className="animate-pulse text-slate-500 font-semibold">Loading...</div>
                                </div>
                            )}
                            <img 
                                className={`h-full w-full object-cover ${!imageLoaded ? 'hidden' : ''}`}
                                src={imageError ? 'https://via.placeholder.com/400x300/cccccc/666666?text=Image+Not+Available' : optimizedSrc}
                                alt={`Sticker ${sticker.id}`}
                                onError={handleImageError}
                                onLoad={handleImageLoad}
                            />
                            {/* Sticker number badge */}
                            <div className="absolute top-2 right-2 bg-white/90 backdrop-blur-sm rounded-full px-2 py-1 text-xs font-bold text-slate-700 shadow-md">
                                #{sticker.id}
                            </div>
                        </div>
                    ) : (
                        <div className="h-full w-full rounded-lg overflow-hidden shadow-lg border-4 border-slate-300 dark:border-slate-700 bg-gradient-to-br from-slate-800 via-slate-700 to-slate-900 relative">
                            {/* Shimmer effect for missing stickers */}
                            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-slate-600/20 to-transparent animate-shimmer" 
                                 style={{ animationDuration: '3s' }}></div>
                            
                            {/* Question mark or lock icon */}
                            <div className="absolute inset-0 flex flex-col items-center justify-center">
                                <div className="text-6xl text-slate-600/50 mb-2">❓</div>
                                <div className="text-sm text-slate-500 font-semibold">Missing Sticker</div>
                                <div className="text-xs text-slate-600 mt-1">#{sticker.id}</div>
                            </div>
                            
                            {/* Decorative corners */}
                            <div className="absolute top-2 left-2 w-4 h-4 border-t-2 border-l-2 border-slate-600/30"></div>
                            <div className="absolute top-2 right-2 w-4 h-4 border-t-2 border-r-2 border-slate-600/30"></div>
                            <div className="absolute bottom-2 left-2 w-4 h-4 border-b-2 border-l-2 border-slate-600/30"></div>
                            <div className="absolute bottom-2 right-2 w-4 h-4 border-b-2 border-r-2 border-slate-600/30"></div>
                        </div>
                    )}
                </div>
                
                {/* Back side */}
                <div
                    className="absolute inset-0 h-full w-full rounded-lg bg-gradient-to-br from-slate-800 via-slate-900 to-black px-6 py-4 text-center text-slate-200 shadow-2xl border-4 border-slate-700 [transform:rotateY(180deg)] [backface-visibility:hidden] overflow-hidden">
                    {/* Decorative background pattern */}
                    <div className="absolute inset-0 opacity-10">
                        <div className="absolute inset-0" style={{
                            backgroundImage: 'radial-gradient(circle, #fff 1px, transparent 1px)',
                            backgroundSize: '15px 15px'
                        }}></div>
                    </div>
                    
                    <div className="relative flex min-h-full flex-col items-center justify-center">
                        <div className="text-3xl mb-2">🦁</div>
                        <h1 className="text-xl font-bold mb-2 text-yellow-400">{sticker.name}</h1>
                        <p className="text-sm italic text-slate-300 mb-3">{sticker.latinName}</p>
                        <p className="text-xs text-slate-400 leading-relaxed max-h-24 overflow-y-auto">{sticker.description}</p>
                        <div className="mt-3 px-3 py-1 bg-slate-700/50 rounded-full text-xs text-slate-300">
                            ID: {sticker.id}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Sticker;