export interface Sticker {
  id: number;
  name: string;
  latinName: string;
  description: string;
  habitat: string;
  src: string;
}

export interface Album {
  id: number;
  name: string;
  stickers: Sticker[];
}

export interface User {
  id?: number;
  username: string;
}
