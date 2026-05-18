import axios from 'axios';
import { Album, AlbumStatus, Sticker } from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add request interceptor for JWT Auth
api.interceptors.request.use(config => {
    // Skip auth for login, registration and public endpoints
    const isPublicEndpoint = 
        config.url?.includes('/auth/') ||
        config.url?.includes('/collectors/username/') || 
        (config.url?.includes('/collectors') && config.method === 'post');
    
    if (!isPublicEndpoint) {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
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
    albumId?: number; // Optional: album the sticker belongs to
}

export interface AlbumResponse {
    id: number;
    name: string;
    status: AlbumStatus;
    stickers: StickerResponse[];
}

export interface AlbumCompletionResponse {
    completion: number;
    missingStickerList: StickerResponse[];
}

export interface StickerWithQuantity {
    sticker: StickerResponse;
    quantity: number;
}

export interface CollectorResponse {
    id: number;
    username: string;
    stars?: number;
    stickers?: StickerWithQuantity[];
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

    cancelTradeOffer: async (senderId: number, receiverId: number, offeredStickerId: number, receivedStickerId: number): Promise<void> => {
        await api.delete(`/tradeoffer/${senderId}/${receiverId}/${offeredStickerId}/${receivedStickerId}`);
    },

    // Notification endpoints
    getNotifications: async (collectorId: number): Promise<any[]> => {
        const response = await api.get(`/notifications/${collectorId}?read=true`);
        return response.data;
    },

    markNotificationAsRead: async (notificationId: number): Promise<void> => {
        await api.put(`/notifications/${notificationId}/read`);
    },
    
    deleteNotification: async (notificationId: number): Promise<void> => {
        await api.delete(`/notifications/${notificationId}`);
    },

    // Admin - Album Management
    createAlbum: async (name: string): Promise<AlbumResponse> => {
        const response = await api.post('/album', { name });
        return response.data;
    },

    updateAlbum: async (
        id: number, 
        name: string, 
        status?: AlbumStatus, 
        stickerIds?: number[]
    ): Promise<AlbumResponse> => {
        const response = await api.put(`/album/${id}`, { name, status, stickerIds });
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

    getStickersPaginated: async (
        page: number = 0,
        size: number = 10,
        sortBy: string = 'id',
        sortDir: string = 'asc',
        search?: string,
        albumId?: number
    ): Promise<{
        stickers: StickerResponse[];
        currentPage: number;
        totalItems: number;
        totalPages: number;
    }> => {
        const params = new URLSearchParams();
        params.append('page', page.toString());
        params.append('size', size.toString());
        params.append('sortBy', sortBy);
        params.append('sortDir', sortDir);
        if (search && search.trim()) {
            params.append('search', search.trim());
        }
        if (albumId !== undefined && albumId !== null) {
            // -1 means "filter for stickers without albums"
            params.append('albumId', albumId.toString());
        }
        const response = await api.get(`/stickers/admin/paginated?${params.toString()}`);
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

    removeStickerFromAlbum: async (id: number): Promise<StickerResponse> => {
        const response = await api.put(`/stickers/${id}/remove-from-album`);
        return response.data;
    },

    // Admin - Collector Management
    getAllCollectors: async (): Promise<CollectorResponse[]> => {
        const response = await api.get('/collectors');
        return response.data;
    },

    getCollectorsPaginated: async (
        page: number = 0,
        size: number = 10,
        sortBy: string = 'id',
        sortDir: string = 'asc',
        search?: string
    ): Promise<{
        collectors: CollectorResponse[];
        currentPage: number;
        totalItems: number;
        totalPages: number;
    }> => {
        const params = new URLSearchParams();
        params.append('page', page.toString());
        params.append('size', size.toString());
        params.append('sortBy', sortBy);
        params.append('sortDir', sortDir);
        if (search && search.trim()) {
            params.append('search', search.trim());
        }
        const response = await api.get(`/collectors/admin/paginated?${params.toString()}`);
        return response.data;
    },

    updateCollector: async (id: number, username: string): Promise<CollectorResponse> => {
        const response = await api.put(`/collectors/${id}`, { username });
        return response.data;
    },

    deleteCollector: async (id: number): Promise<void> => {
        await api.delete(`/collectors/${id}`);
    },

    // Album Completion endpoints
    completeAlbum: async (albumId: number, collectorId: number): Promise<void> => {
        await api.post(`/album/${albumId}/complete/collector/${collectorId}`);
    },

    // Ranking endpoints
    getMonthlyRanking: async (
        year: number,
        month: number,
        page: number = 0,
        size: number = 10,
        search?: string
    ): Promise<PagedRankingResponse> => {
        const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
        if (search) params.append('search', search);
        const response = await api.get(`/rankings/monthly/${year}/${month}?${params}`);
        return response.data;
    },

    getYearlyRanking: async (
        year: number,
        page: number = 0,
        size: number = 10,
        search?: string
    ): Promise<PagedRankingResponse> => {
        const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
        if (search) params.append('search', search);
        const response = await api.get(`/rankings/yearly/${year}?${params}`);
        return response.data;
    },

    getAllTimeRanking: async (
        page: number = 0,
        size: number = 10,
        search?: string
    ): Promise<PagedRankingResponse> => {
        const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
        if (search) params.append('search', search);
        const response = await api.get(`/rankings/all-time?${params}`);
        return response.data;
    },
};

export interface PagedRankingResponse {
    rankings: RankingEntry[];
    currentPage: number;
    totalPages: number;
    totalItems: number;
    pageSize: number;
}

export interface RankingEntry {
    collectorId: number;
    username: string;
    completions: number;
    stars: number;
    score: number;
}

export default apiService;
