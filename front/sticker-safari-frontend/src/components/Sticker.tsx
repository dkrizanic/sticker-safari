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
        <div className="group h-40 w-80 ml-10 mt-5">
            <div
                className="relative h-full w-full transition-all duration-500 [transform-style:preserve-3d] group-hover:[transform:rotateY(180deg)]">
                <div className="absolute inset-0">
                    {!hidden ? (
                        <>
                            {!imageLoaded && (
                                <div className="h-full w-full flex items-center justify-center bg-gray-200 border border-black p-1">
                                    <div className="animate-pulse text-gray-400">Loading...</div>
                                </div>
                            )}
                            <img 
                                className={`h-full w-full object-cover border border-black p-1 ${!imageLoaded ? 'hidden' : ''}`}
                                src={imageError ? 'https://via.placeholder.com/400x300/cccccc/666666?text=Image+Not+Available' : optimizedSrc}
                                alt={`Sticker ${sticker.id}`}
                                onError={handleImageError}
                                onLoad={handleImageLoad}
                            />
                        </>
                    ) : (
                        <div className="h-full w-full object-cover bg-black border border-black p-1"></div>
                    )}
                </div>
                <div
                    className="absolute inset-0 h-full w-full bg-black/80 px-12 text-center text-slate-200 [transform:rotateY(180deg)] [backface-visibility:hidden]">
                    <div className="flex min-h-full flex-col items-center justify-center">
                        <h1 className="text-lg font-bold">{sticker.name}</h1>
                        <p className="text-md">{sticker.latinName}</p>
                        <p className="word-break text-sm">{sticker.description}</p>
                        <p className="word-break text-sm">{sticker.id}</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Sticker;