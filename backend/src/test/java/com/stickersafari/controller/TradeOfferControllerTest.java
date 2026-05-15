package com.stickersafari.controller;

import com.stickersafari.dto.*;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Sticker;
import com.stickersafari.entity.TradeOffer;
import com.stickersafari.entity.TradeOfferStatus;
import com.stickersafari.mapper.TradeOfferMapper;
import com.stickersafari.service.TradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeOfferControllerTest {

    @Mock
    private TradingService tradingService;

    @Mock
    private TradeOfferMapper tradeOfferMapper;

    @InjectMocks
    private TradeOfferController tradeOfferController;

    private TradeOffer testTradeOffer;
    private TradeOfferResponse testTradeOfferResponse;
    private SpecificTradeOfferRequest specificRequest;
    private GlobalTradeOfferRequest globalRequest;
    private AnswerPendingTradeOfferRequest answerRequest;

    @BeforeEach
    void setUp() {
        Collector sender = new Collector();
        sender.setId(1L);
        sender.setUsername("sender");

        Collector receiver = new Collector();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        Sticker offeredSticker = new Sticker();
        offeredSticker.setId(1L);
        offeredSticker.setName("Sticker 1");

        Sticker receivedSticker = new Sticker();
        receivedSticker.setId(2L);
        receivedSticker.setName("Sticker 2");

        testTradeOffer = new TradeOffer();
        testTradeOffer.setSender(sender);
        testTradeOffer.setReceiver(receiver);
        testTradeOffer.setOfferedSticker(offeredSticker);
        testTradeOffer.setReceivedSticker(receivedSticker);
        testTradeOffer.setStatus(TradeOfferStatus.PENDING);
        testTradeOffer.setMessage("Trade request");

        testTradeOfferResponse = new TradeOfferResponse();
        testTradeOfferResponse.setStatus("PENDING");
        testTradeOfferResponse.setMessage("Trade request");

        specificRequest = new SpecificTradeOfferRequest();
        specificRequest.setSenderId(1L);
        specificRequest.setReceiverId(2L);
        specificRequest.setOfferedStickerId(1L);
        specificRequest.setReceivedStickerId(2L);

        globalRequest = new GlobalTradeOfferRequest();
        globalRequest.setSenderId(1L);
        globalRequest.setOfferedStickerId(1L);
        globalRequest.setReceivedStickerId(2L);

        answerRequest = new AnswerPendingTradeOfferRequest();
        answerRequest.setSenderId(1L);
        answerRequest.setReceiverId(2L);
        answerRequest.setOfferedStickerId(1L);
        answerRequest.setReceivedStickerId(2L);
    }

    @Test
    void manualTrade_ShouldReturnCreatedStatus() {
        // Given
        when(tradingService.sendSpecificTradeOffer(any(SpecificTradeOfferRequest.class))).thenReturn(testTradeOffer);
        when(tradeOfferMapper.toResponse(testTradeOffer)).thenReturn(testTradeOfferResponse);

        // When
        ResponseEntity<TradeOfferResponse> result = tradeOfferController.manualTrade(specificRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatus()).isEqualTo("PENDING");
        verify(tradingService).sendSpecificTradeOffer(specificRequest);
        verify(tradeOfferMapper).toResponse(testTradeOffer);
    }

    @Test
    void globalTrade_ShouldReturnCreatedStatus() {
        // Given
        String expectedMessage = "Sent trade offers to 5 potential partners";
        when(tradingService.sendGlobalTradeOffer(any(GlobalTradeOfferRequest.class))).thenReturn(expectedMessage);

        // When
        ResponseEntity<String> result = tradeOfferController.globalTrade(globalRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(expectedMessage);
        verify(tradingService).sendGlobalTradeOffer(globalRequest);
    }

    @Test
    void getPendingReceivedTradeOffers_ShouldReturnListOfOffers() {
        // Given
        List<TradeOffer> tradeOffers = List.of(testTradeOffer);
        List<TradeOfferResponse> responses = List.of(testTradeOfferResponse);
        
        when(tradingService.getPendingReceivedTradeOffers(2L)).thenReturn(tradeOffers);
        when(tradeOfferMapper.toResponseList(tradeOffers)).thenReturn(responses);

        // When
        ResponseEntity<List<TradeOfferResponse>> result = tradeOfferController.getPendingReceivedTradeOffers(2L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        verify(tradingService).getPendingReceivedTradeOffers(2L);
        verify(tradeOfferMapper).toResponseList(tradeOffers);
    }

    @Test
    void getPendingSentTradeOffers_ShouldReturnListOfOffers() {
        // Given
        List<TradeOffer> tradeOffers = List.of(testTradeOffer);
        List<TradeOfferResponse> responses = List.of(testTradeOfferResponse);
        
        when(tradingService.getPendingSentTradeOffers(1L)).thenReturn(tradeOffers);
        when(tradeOfferMapper.toResponseList(tradeOffers)).thenReturn(responses);

        // When
        ResponseEntity<List<TradeOfferResponse>> result = tradeOfferController.getPendingSentTradeOffers(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        verify(tradingService).getPendingSentTradeOffers(1L);
        verify(tradeOfferMapper).toResponseList(tradeOffers);
    }

    @Test
    void acceptPendingTradeOffer_ShouldReturnAcceptedOffer() throws Exception {
        // Given
        testTradeOffer.setStatus(TradeOfferStatus.ACCEPTED);
        testTradeOfferResponse.setStatus("ACCEPTED");
        
        when(tradingService.acceptTradeOffer(any(AnswerPendingTradeOfferRequest.class))).thenReturn(testTradeOffer);
        when(tradeOfferMapper.toResponse(testTradeOffer)).thenReturn(testTradeOfferResponse);

        // When
        ResponseEntity<TradeOfferResponse> result = tradeOfferController.acceptPendingTradeOffer(answerRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatus()).isEqualTo("ACCEPTED");
        verify(tradingService).acceptTradeOffer(answerRequest);
        verify(tradeOfferMapper).toResponse(testTradeOffer);
    }

    @Test
    void refusePendingTradeOffer_ShouldReturnRefusedOffer() {
        // Given
        testTradeOffer.setStatus(TradeOfferStatus.REFUSED);
        testTradeOfferResponse.setStatus("REFUSED");
        
        when(tradingService.refusePendingTradeOffer(any(AnswerPendingTradeOfferRequest.class))).thenReturn(testTradeOffer);
        when(tradeOfferMapper.toResponse(testTradeOffer)).thenReturn(testTradeOfferResponse);

        // When
        ResponseEntity<TradeOfferResponse> result = tradeOfferController.refusePendingTradeOffer(answerRequest);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getStatus()).isEqualTo("REFUSED");
        verify(tradingService).refusePendingTradeOffer(answerRequest);
        verify(tradeOfferMapper).toResponse(testTradeOffer);
    }

    @Test
    void getPendingReceivedTradeOffers_ShouldReturnEmptyList_WhenNoOffers() {
        // Given
        when(tradingService.getPendingReceivedTradeOffers(2L)).thenReturn(new ArrayList<>());
        when(tradeOfferMapper.toResponseList(any())).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<TradeOfferResponse>> result = tradeOfferController.getPendingReceivedTradeOffers(2L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEmpty();
        verify(tradingService).getPendingReceivedTradeOffers(2L);
    }
}
