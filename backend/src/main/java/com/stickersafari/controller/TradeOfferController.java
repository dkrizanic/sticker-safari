package com.stickersafari.controller;

import com.stickersafari.dto.*;
import com.stickersafari.entity.TradeOffer;
import com.stickersafari.mapper.TradeOfferMapper;
import com.stickersafari.service.TradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tradeoffer")
@RequiredArgsConstructor
@Slf4j
public class TradeOfferController {

    private final TradingService tradingService;
    private final TradeOfferMapper tradeOfferMapper;

    @PostMapping("/specific")
    public ResponseEntity<TradeOfferResponse> manualTrade(@Valid @RequestBody SpecificTradeOfferRequest request) {
        log.info("Received manual trade request from {} to {}", request.getSenderId(), request.getReceiverId());
        TradeOfferResponse response = tradeOfferMapper.toResponse(tradingService.sendSpecificTradeOffer(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/global")
    public ResponseEntity<String> globalTrade(@Valid @RequestBody GlobalTradeOfferRequest request) {
        log.info("Received global trade request from {}", request.getSenderId());
        String result = tradingService.sendGlobalTradeOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/received/{collectorId}")
    public ResponseEntity<List<TradeOfferResponse>> getPendingReceivedTradeOffers(@PathVariable("collectorId") Long collectorId) {
        log.debug("Getting received trade offers for collector {}", collectorId);
        List<TradeOffer> tradeOffers = tradingService.getPendingReceivedTradeOffers(collectorId);
        return ResponseEntity.ok(tradeOfferMapper.toResponseList(tradeOffers));
    }

    @GetMapping("/sent/{collectorId}")
    public ResponseEntity<List<TradeOfferResponse>> getPendingSentTradeOffers(@PathVariable("collectorId") Long collectorId) {
        log.debug("Getting sent trade offers for collector {}", collectorId);
        List<TradeOffer> tradeOffers = tradingService.getPendingSentTradeOffers(collectorId);
        return ResponseEntity.ok(tradeOfferMapper.toResponseList(tradeOffers));
    }

    @PostMapping("/requests/accept")
    public ResponseEntity<TradeOfferResponse> acceptPendingTradeOffer(@Valid @RequestBody AnswerPendingTradeOfferRequest request) {
        log.info("Accepting trade offer request");
        TradeOfferResponse response = tradeOfferMapper.toResponse(tradingService.acceptTradeOffer(request));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requests/refuse")
    public ResponseEntity<TradeOfferResponse> refusePendingTradeOffer(@Valid @RequestBody AnswerPendingTradeOfferRequest request) {
        log.info("Refusing trade offer request");
        TradeOfferResponse response = tradeOfferMapper.toResponse(tradingService.refusePendingTradeOffer(request));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> cancelTradeOffer(@Valid CancelTradeOfferRequest request) {
        log.info("Canceling trade offer from {} to {}", request.getSenderId(), request.getReceiverId());
        tradingService.cancelTradeOffer(
            request.getSenderId(), 
            request.getReceiverId(), 
            request.getOfferedStickerId(), 
            request.getReceivedStickerId());
        return ResponseEntity.noContent().build();
    }
}
