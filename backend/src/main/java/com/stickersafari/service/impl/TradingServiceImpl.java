package com.stickersafari.service.impl;

import com.stickersafari.dto.AnswerPendingTradeOfferRequest;
import com.stickersafari.dto.GlobalTradeOfferRequest;
import com.stickersafari.dto.SpecificTradeOfferRequest;
import com.stickersafari.entity.*;
import com.stickersafari.enums.TradeOfferStatus;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.exception.TradeOfferException;
import com.stickersafari.repository.TradeOfferRepository;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.NotificationService;
import com.stickersafari.service.StickerService;
import com.stickersafari.service.TradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class TradingServiceImpl implements TradingService {
    private final CollectorService collectorService;
    private final StickerService stickerService;
    private final TradeOfferRepository tradeOfferRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TradeOffer sendSpecificTradeOffer(SpecificTradeOfferRequest request) {
        log.info("Sending specific trade offer from {} to {}", request.getSenderId(), request.getReceiverId());
        Collector receiver = collectorService.findById(request.getReceiverId());
        Collector sender = collectorService.findById(request.getSenderId());

        TradeOffer tradeOffer = mapSpecificTradeOfferRequestToTradeOffer(request, sender, receiver);
        tradeOfferRepository.save(tradeOffer);

        createAndSaveNotification(receiver, "You have received a trade offer from " + sender.getUsername());

        return tradeOffer;
    }

    @Override
    @Transactional
    public String sendGlobalTradeOffer(GlobalTradeOfferRequest request) {
        log.info("Sending global trade offer from collector {}", request.getSenderId());
        Collector sender = collectorService.findById(request.getSenderId());

        boolean senderHasOfferedSticker = sender.getOwnedStickers().containsKey(request.getOfferedStickerId());
        if (!senderHasOfferedSticker) {
            throw new TradeOfferException("Sender does not own the offered sticker");
        }
        
        // Fetch stickers once to avoid N+1 queries
        Sticker offeredSticker = stickerService.findOne(request.getOfferedStickerId());
        Sticker receivedSticker = stickerService.findOne(request.getReceivedStickerId());
        
        List<Collector> collectors = collectorService.findPotentialTradingPartners(
            request.getSenderId(), request.getOfferedStickerId(), request.getReceivedStickerId());
        log.debug("Found {} potential trading partners", collectors.size());

        for (Collector collector : collectors) {
            SpecificTradeOfferRequest specificTradeOfferRequest = new SpecificTradeOfferRequest();
            specificTradeOfferRequest.setSenderId(sender.getId());
            specificTradeOfferRequest.setReceiverId(collector.getId());
            specificTradeOfferRequest.setOfferedStickerId(request.getOfferedStickerId());
            specificTradeOfferRequest.setReceivedStickerId(request.getReceivedStickerId());
            specificTradeOfferRequest.setMessage(String.format(
                "Hello, I would like to trade you my sticker %d: %s for your %d: %s",
                request.getOfferedStickerId(), offeredSticker.getName(),
                request.getReceivedStickerId(), receivedSticker.getName()));
            
            TradeOffer tradeOffer = mapSpecificTradeOfferRequestToTradeOffer(specificTradeOfferRequest, sender, collector);
            tradeOfferRepository.save(tradeOffer);
            
            createAndSaveNotification(collector, "You have received a trade offer from " + sender.getUsername());
        }
        
        log.info("Sent {} global trade offers", collectors.size());
        return "Sent trade offers to " + collectors.size() + " potential partners";
    }

    @Override
    public List<TradeOffer> getPendingReceivedTradeOffers(Long collectorId) {
        return tradeOfferRepository.findByReceiverIdAndStatus(collectorId, TradeOfferStatus.PENDING);
    }

    @Override
    public List<TradeOffer> getPendingSentTradeOffers(Long collectorId) {
        return tradeOfferRepository.findBySenderIdAndStatus(collectorId, TradeOfferStatus.PENDING);
    }

    @Override
    @Transactional
    public TradeOffer acceptTradeOffer(AnswerPendingTradeOfferRequest request) {
        log.info("Accepting trade offer: sender={}, receiver={}", request.getSenderId(), request.getReceiverId());
        
        TradeOffer tradeOffer = tradeOfferRepository.findTradeOffer(
                request.getSenderId(), request.getReceiverId(), 
                request.getOfferedStickerId(), request.getReceivedStickerId());
        
        if (tradeOffer == null) {
            throw new EntityNotFoundException("Trade offer not found");
        }

        Collector sender = collectorService.findById(request.getSenderId());
        Collector receiver = collectorService.findById(request.getReceiverId());

        // Execute the trade using service methods
        collectorService.removeStickerFromCollector(sender, request.getOfferedStickerId());
        collectorService.addStickerToCollector(sender, request.getReceivedStickerId());
        collectorService.save(sender);

        collectorService.removeStickerFromCollector(receiver, request.getReceivedStickerId());
        collectorService.addStickerToCollector(receiver, request.getOfferedStickerId());
        collectorService.save(receiver);

        tradeOffer.setStatus(TradeOfferStatus.ACCEPTED);

        createAndSaveNotification(sender, receiver.getUsername() + " accepted your trade offer");
        
        log.info("Trade offer accepted successfully");
        return tradeOfferRepository.save(tradeOffer);
    }

    @Override
    @Transactional
    public TradeOffer refusePendingTradeOffer(AnswerPendingTradeOfferRequest request) {
        log.info("Refusing trade offer: sender={}, receiver={}", request.getSenderId(), request.getReceiverId());
        
        TradeOffer tradeOffer = tradeOfferRepository.findTradeOffer(
                request.getSenderId(), request.getReceiverId(), 
                request.getOfferedStickerId(), request.getReceivedStickerId());
        
        if (tradeOffer == null) {
            throw new EntityNotFoundException("Trade offer not found");
        }

        Collector sender = collectorService.findById(request.getSenderId());
        Collector receiver = collectorService.findById(request.getReceiverId());

        tradeOffer.setStatus(TradeOfferStatus.REFUSED);

        createAndSaveNotification(sender, receiver.getUsername() + " refused your trade offer");
        
        log.info("Trade offer refused");
        return tradeOfferRepository.save(tradeOffer);
    }
    
    private void createAndSaveNotification(Collector recipient, String message) {
        Notification notification = new Notification();
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notificationService.saveNotification(notification);
    }

    private TradeOffer mapSpecificTradeOfferRequestToTradeOffer(SpecificTradeOfferRequest request, Collector sender, Collector receiver) {
        TradeOffer tradeOffer = new TradeOffer();

        TradeOfferKey tradeOfferKey = new TradeOfferKey(request.getSenderId(), request.getReceiverId(), request.getOfferedStickerId(), request.getReceivedStickerId());
        tradeOffer.setId(tradeOfferKey);
        tradeOffer.setSender(sender);
        tradeOffer.setReceiver(receiver);
        tradeOffer.setOfferedSticker(stickerService.findOne(request.getOfferedStickerId()));
        tradeOffer.setReceivedSticker(stickerService.findOne(request.getReceivedStickerId()));
        tradeOffer.setMessage(request.getMessage());
        tradeOffer.setStatus(TradeOfferStatus.PENDING);

        return tradeOffer;
    }

    @Override
    @Transactional
    public void cancelTradeOffer(Long senderId, Long receiverId, Long offeredStickerId, Long receivedStickerId) {
        log.info("Canceling trade offer: sender={}, receiver={}, offered={}, received={}", 
                senderId, receiverId, offeredStickerId, receivedStickerId);
        
        TradeOffer tradeOffer = tradeOfferRepository.findTradeOffer(
                senderId, receiverId, offeredStickerId, receivedStickerId);
        
        if (tradeOffer == null) {
            throw new EntityNotFoundException("Trade offer not found");
        }
        
        if (tradeOffer.getStatus() != TradeOfferStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending trade offers");
        }
        
        tradeOfferRepository.delete(tradeOffer);
        log.info("Trade offer canceled successfully");
    }
}
