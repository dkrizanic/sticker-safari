import React from 'react';
import { Sticker as StickerType } from '../types';

interface StickerProps {
  sticker: StickerType;
  hidden: boolean;
}

function Sticker({ sticker, hidden }: StickerProps): JSX.Element {

    return (
        <div className="group h-40 w-80 ml-10 mt-5">
            <div
                className="relative h-full w-full transition-all duration-500 [transform-style:preserve-3d] group-hover:[transform:rotateY(180deg)]">
                <div className="absolute inset-0">
                    {!hidden ? <img className="h-full w-full object-cover border border-black p-1"
                                    src={sticker.src}
                                    alt={`Sticker ${sticker.id}`}/> :
                            <div className="h-full w-full object-cover bg-black border border-black p-1"></div>}
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