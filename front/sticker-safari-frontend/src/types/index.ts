export interface Sticker {
  id: number;
  name: string;
  latinName: string;
  description: string;
  habitat: string;
  src: string;
}

export enum AlbumStatus {
  PENDING = 'PENDING',
  PUBLISHED = 'PUBLISHED'
}

export interface Album {
  id: number;
  name: string;
  status: AlbumStatus;
  stickers: Sticker[];
}

export interface User {
  id?: number;
  username: string;
  role?: 'COLLECTOR' | 'ADMIN';
}
