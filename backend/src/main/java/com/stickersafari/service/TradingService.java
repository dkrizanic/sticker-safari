package com.stickersafari.service;

import com.stickersafari.dto.AnswerPendingTradeOfferRequest;
import com.stickersafari.dto.GlobalTradeOfferRequest;
import com.stickersafari.dto.SpecificTradeOfferRequest;
import com.stickersafari.entity.TradeOffer;

import java.util.List;

public interface TradingService {
    TradeOffer sendSpecificTradeOffer(SpecificTradeOfferRequest specificTradeRequest);

    String sendGlobalTradeOffer(GlobalTradeOfferRequest globalTradeRequest);

    List<TradeOffer> getPendingReceivedTradeOffers(Long collectorId);

    List<TradeOffer> getPendingSentTradeOffers(Long collectorId);

    TradeOffer acceptTradeOffer(AnswerPendingTradeOfferRequest request) throws Exception;

    TradeOffer refusePendingTradeOffer(AnswerPendingTradeOfferRequest request);

    void cancelTradeOffer(Long senderId, Long receiverId, Long offeredStickerId, Long receivedStickerId);
}
