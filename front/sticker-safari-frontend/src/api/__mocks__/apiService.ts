export const getAlbum = jest.fn();
export const getAlbumCompletion = jest.fn();
export const openPack = jest.fn();
export const getCollector = jest.fn();
export const getCollectorByUsername = jest.fn();
export const sendSpecificTradeOffer = jest.fn();
export const sendGlobalTradeOffer = jest.fn();
export const getReceivedTradeOffers = jest.fn();
export const getSentTradeOffers = jest.fn();
export const acceptTradeOffer = jest.fn();
export const refuseTradeOffer = jest.fn();
export const getNotifications = jest.fn();

const apiService = {
  getAlbum,
  getAlbumCompletion,
  openPack,
  getCollector,
  getCollectorByUsername,
  sendSpecificTradeOffer,
  sendGlobalTradeOffer,
  getReceivedTradeOffers,
  getSentTradeOffers,
  acceptTradeOffer,
  refuseTradeOffer,
  getNotifications,
};

export default apiService;
