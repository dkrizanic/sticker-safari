import axios from 'axios';
import { Album, Sticker } from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add request interceptor for Basic Auth
api.interceptors.request.use(config => {
    // Skip auth for login and registration endpoints
    const isPublicEndpoint = 
        config.url?.includes('/collectors/username/') || 
        (config.url?.includes('/collectors') && config.method === 'post');
    
    if (!isPublicEndpoint) {
        const username = localStorage.getItem('username');
        const password = sessionStorage.getItem('password') || 'collector';
        if (username) {
            const credentials = btoa(`${username}:${password}`);
            config.headers.Authorization = `Basic ${credentials}`;
        }
    }
    return config;
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
    getAllAlbums: async (): Promise<AlbumResponse[]> => {
        const response = await api.get('/album');
        return response.data;
    },
    
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

    getCollectorStickers: async (collectorId: number): Promise<number[]> => {
        const response = await api.get(`/collectors/${collectorId}/stickers`);
        return response.data;
    },

    createCollector: async (username: string): Promise<CollectorResponse> => {
        const response = await api.post('/collectors', { username });
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
        const response = await api.get(`/notifications/${collectorId}?read=true`);
        return response.data;
    },

    markNotificationAsRead: async (notificationId: number): Promise<void> => {
        await api.put(`/notifications/${notificationId}/read`);
    },

    // Admin - Album Management
    createAlbum: async (name: string): Promise<AlbumResponse> => {
        const response = await api.post('/album', { name });
        return response.data;
    },

    updateAlbum: async (id: number, name: string): Promise<AlbumResponse> => {
        const response = await api.put(`/album/${id}`, { name });
        return response.data;
    },

    deleteAlbum: async (id: number): Promise<void> => {
        await api.delete(`/album/${id}`);
    },

    // Admin - Sticker Management
    getAllStickers: async (): Promise<StickerResponse[]> => {
        const response = await api.get('/stickers');
        return response.data;
    },

    createSticker: async (sticker: Omit<StickerResponse, 'id'>): Promise<StickerResponse> => {
        const response = await api.post('/stickers', sticker);
        return response.data;
    },

    updateSticker: async (id: number, sticker: Partial<Omit<StickerResponse, 'id'>>): Promise<StickerResponse> => {
        const response = await api.put(`/stickers/${id}`, sticker);
        return response.data;
    },

    // Admin - Collector Management
    getAllCollectors: async (): Promise<CollectorResponse[]> => {
        const response = await api.get('/collectors');
        return response.data;
    },

    updateCollector: async (id: number, username: string): Promise<CollectorResponse> => {
        const response = await api.put(`/collectors/${id}`, { username });
        return response.data;
    },

    deleteCollector: async (id: number): Promise<void> => {
        await api.delete(`/collectors/${id}`);
    },
};

export default apiService;
