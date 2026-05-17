import React from 'react';
import Sticker from './Sticker';
import { Sticker as StickerType } from '../types';

interface AlbumStickersProps {
  albumStickers: StickerType[];
  ownedStickers: number[];
}

function AlbumStickers({ albumStickers, ownedStickers }: AlbumStickersProps): JSX.Element {
    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-6 p-2">
            {albumStickers.map((sticker) => (
                <Sticker key={sticker.id} sticker={sticker} hidden={!ownedStickers.includes(sticker.id)} />
            ))}
        </div>
    );
}

export default AlbumStickers;