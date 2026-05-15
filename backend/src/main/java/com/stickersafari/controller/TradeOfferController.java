package com.stickersafari.controller;

import com.stickersafari.dto.*;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.entity.TradeOffer;
import com.stickersafari.service.TradingService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("tradeoffer")
public class TradeOfferController {

    private final TradingService tradingService;

    public TradeOfferController(TradingService tradingService) {
        this.tradingService = tradingService;
    }


    @PostMapping("/specific")
    public TradeOfferResponse manualTrade(@RequestBody SpecificTradeOfferRequest request) {
        return mapTradeOfferToTradeOfferResponse(tradingService.sendSpecificTradeOffer(request));
    }

    @PostMapping("/global")
    public String globalTrade(@RequestBody GlobalTradeOfferRequest request) {
        return tradingService.sendGlobalTradeOffer(request);
    }

    @GetMapping("/received/{collectorId}")
    public List<TradeOfferResponse> getPendingReceivedTradeOffers(@PathVariable("collectorId") Long collectorId) {
       List<TradeOffer> tradeOffers = tradingService.getPendingReceivedTradeOffers(collectorId);
       return mapTradeOffersToTradeOfferResponses(tradeOffers);
    }

    @GetMapping("/sent/{collectorId}")
    public List<TradeOfferResponse> getPendingSentTradeOffers(@PathVariable("collectorId") Long collectorId) {
            List<TradeOffer> tradeOffers = tradingService.getPendingSentTradeOffers(collectorId);
        return mapTradeOffersToTradeOfferResponses(tradeOffers);
    }

    @PostMapping("/requests/accept")
    public TradeOfferResponse acceptPendingTradeOffer(@RequestBody AnswerPendingTradeOfferRequest request) throws Exception {
        return mapTradeOfferToTradeOfferResponse(tradingService.acceptTradeOffer(request));
    }

    @PostMapping("/requests/refuse")
    public TradeOfferResponse refusePendingTradeOffer(@RequestBody AnswerPendingTradeOfferRequest request) {
        return mapTradeOfferToTradeOfferResponse(tradingService.refusePendingTradeOffer(request));
    }

    private List<TradeOfferResponse> mapTradeOffersToTradeOfferResponses(List<TradeOffer> tradeOffers){
        List<TradeOfferResponse> tradeOfferResponses = new ArrayList<>();

        for(TradeOffer tradeOffer : tradeOffers){
            TradeOfferResponse tradeOfferResponse = new TradeOfferResponse();

            tradeOfferResponse.setSender(mapCollectorToCollectorResponse(tradeOffer.getSender()));
            tradeOfferResponse.setReceiver(mapCollectorToCollectorResponse(tradeOffer.getReceiver()));
            tradeOfferResponse.setOfferedSticker(mapStickerToStickerResponse(tradeOffer.getOfferedSticker()));
            tradeOfferResponse.setReceivedSticker(mapStickerToStickerResponse(tradeOffer.getReceivedSticker()));
            tradeOfferResponse.setMessage(tradeOffer.getMessage());
            tradeOfferResponse.setStatus(tradeOffer.getStatus());

            tradeOfferResponses.add(tradeOfferResponse);
        }

        return tradeOfferResponses;
    }

    private TradeOfferResponse mapTradeOfferToTradeOfferResponse(TradeOffer tradeOffer){
        TradeOfferResponse tradeOfferResponse = new TradeOfferResponse();

        tradeOfferResponse.setSender(mapCollectorToCollectorResponse(tradeOffer.getSender()));
        tradeOfferResponse.setReceiver(mapCollectorToCollectorResponse(tradeOffer.getReceiver()));
        tradeOfferResponse.setOfferedSticker(mapStickerToStickerResponse(tradeOffer.getOfferedSticker()));
        tradeOfferResponse.setReceivedSticker(mapStickerToStickerResponse(tradeOffer.getReceivedSticker()));
        tradeOfferResponse.setMessage(tradeOffer.getMessage());
        tradeOfferResponse.setStatus(tradeOffer.getStatus());

        return tradeOfferResponse;
    }

    private CollectorResponse mapCollectorToCollectorResponse(Collector collector) {
        CollectorResponse collectorResponse = new CollectorResponse();

        collectorResponse.setUsername(collector.getUsername());

        return collectorResponse;
    }

    private StickerResponse mapStickerToStickerResponse(Sticker sticker) {
        StickerResponse stickerResponse = new StickerResponse();

        stickerResponse.setName(sticker.getName());
        stickerResponse.setLatinName(sticker.getLatinName());
        stickerResponse.setHabitat(sticker.getHabitat());
        stickerResponse.setDescription(sticker.getDescription());
        stickerResponse.setId(sticker.getId());

        return stickerResponse;
    }
}
