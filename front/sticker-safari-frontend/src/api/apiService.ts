import axios from 'axios';
import { Album, Sticker } from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export interface StickerResponse {
    id: number;
    name: string;
    latinName: string;
    description: string;
    habitat: string;
    src: string;
}

export interface AlbumResponse {
    id: number;
    name: string;
    stickers: StickerResponse[];
}

export interface AlbumCompletionResponse {
    completion: number;
    missingStickerList: StickerResponse[];
}

export interface CollectorResponse {
    id: number;
    username: string;
}

export interface TradeOfferResponse {
    sender: CollectorResponse;
    receiver: CollectorResponse;
    offeredSticker: StickerResponse;
    receivedSticker: StickerResponse;
    message: string;
    status: string;
}

export interface SpecificTradeOfferRequest {
    senderId: number;
    receiverId: number;
    offeredStickerId: number;
    receivedStickerId: number;
    message?: string;
}

export interface GlobalTradeOfferRequest {
    senderId: number;
    offeredStickerId: number;
    receivedStickerId: number;
    message?: string;
}

export interface AnswerPendingTradeOfferRequest {
    senderId: number;
    receiverId: number;
    offeredStickerId: number;
    receivedStickerId: number;
}

const apiService = {
    // Album endpoints
    getAlbum: async (albumId: number): Promise<AlbumResponse> => {
        const response = await api.get(`/album/${albumId}`);
        return response.data;
    },

    getAlbumCompletion: async (albumId: number, collectorId: number): Promise<AlbumCompletionResponse> => {
        const response = await api.get(`/album/${albumId}/collector/${collectorId}`);
        return response.data;
    },

    // Sticker endpoints
    openPack: async (albumId: number, userId: number, size: number = 3): Promise<StickerResponse[]> => {
        const response = await api.post(`/stickers/open/${albumId}/collector/${userId}?size=${size}`);
        return response.data;
    },

    // Collector endpoints
    getCollector: async (id: number): Promise<CollectorResponse> => {
        const response = await api.get(`/collectors/${id}`);
        return response.data;
    },

    getCollectorByUsername: async (username: string): Promise<CollectorResponse> => {
        const response = await api.get(`/collectors/username/${username}`);
        return response.data;
    },

    // Trade Offer endpoints
    sendSpecificTradeOffer: async (request: SpecificTradeOfferRequest): Promise<TradeOfferResponse> => {
        const response = await api.post('/tradeoffer/specific', request);
        return response.data;
    },

    sendGlobalTradeOffer: async (request: GlobalTradeOfferRequest): Promise<string> => {
        const response = await api.post('/tradeoffer/global', request);
        return response.data;
    },

    getReceivedTradeOffers: async (collectorId: number): Promise<TradeOfferResponse[]> => {
        const response = await api.get(`/tradeoffer/received/${collectorId}`);
        return response.data;
    },

    getSentTradeOffers: async (collectorId: number): Promise<TradeOfferResponse[]> => {
        const response = await api.get(`/tradeoffer/sent/${collectorId}`);
        return response.data;
    },

    acceptTradeOffer: async (request: AnswerPendingTradeOfferRequest): Promise<TradeOfferResponse> => {
        const response = await api.post('/tradeoffer/requests/accept', request);
        return response.data;
    },

    refuseTradeOffer: async (request: AnswerPendingTradeOfferRequest): Promise<TradeOfferResponse> => {
        const response = await api.post('/tradeoffer/requests/refuse', request);
        return response.data;
    },

    // Notification endpoints
    getNotifications: async (collectorId: number): Promise<any[]> => {
        const response = await api.get(`/notification/${collectorId}`);
        return response.data;
    },
};

export default apiService;
