import React, { useState } from 'react';
import AlbumStickers from './AlbumStickers';
import { ReactComponent as ArrowUp } from "./assets/svg/up-arrow.svg";
import { ReactComponent as ArrowDown } from "./assets/svg/down-arrow.svg";
import { Album } from '../types';

interface AlbumCardProps {
  album: Album;
  ownedStickers: number[];
}

function AlbumCard({ album, ownedStickers }: AlbumCardProps): JSX.Element {
    const [isHidden, setIsHidden] = useState<boolean>(true);

    const handleArrowClick = () => {
        setIsHidden(!isHidden);
    };
    
    return (
        <div className="py-2">
            <div
                className="flex flex-row justify-between block max-w-sm p-6 bg-white border border-gray-200 rounded-lg shadow hover:bg-gray-100 dark:bg-gray-800 dark:border-gray-700 dark:hover:bg-gray-700 cursor-pointer"
                onClick={handleArrowClick()}>
                <h3>{album.name}</h3>
                {isHidden ?
                    <ArrowUp className='w-5 h-5 ml-2'/> :
                    <ArrowDown className='w-5 h-5 ml-2'/>}
            </div>
            {!isHidden && (
                <AlbumStickers albumStickers = {album.stickers} ownedStickers={ownedStickers}/>
            )}
        </div>
    )
}

export default AlbumCard;