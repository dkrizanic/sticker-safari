import React from 'react';
import Sticker from './Sticker';
import { Sticker as StickerType } from '../types';

interface AlbumStickersProps {
  albumStickers: StickerType[];
  ownedStickers: number[];
}

function AlbumStickers({ albumStickers, ownedStickers }: AlbumStickersProps): JSX.Element {
    return (
        <div className="flex flex-wrap">
            {albumStickers.map((sticker) => (
                <Sticker sticker={sticker} hidden={!ownedStickers.includes(sticker.id)} />
            ))}
        </div>
    );
}

export default AlbumStickers;