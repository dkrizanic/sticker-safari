package com.stickersafari.service.impl;

import com.stickersafari.dto.AnswerPendingTradeOfferRequest;
import com.stickersafari.dto.GlobalTradeOfferRequest;
import com.stickersafari.dto.SpecificTradeOfferRequest;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.Notification;
import com.stickersafari.entity.TradeOffer;
import com.stickersafari.entity.TradeOfferKey;
import com.stickersafari.repository.TradeOfferRepository;
import com.stickersafari.service.CollectorService;
import com.stickersafari.service.NotificationService;
import com.stickersafari.service.StickerService;
import com.stickersafari.service.TradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TradingServiceImpl implements TradingService {
    private final CollectorService collectorService;
    private final StickerService stickerService;
    private final TradeOfferRepository tradeOfferRepository;

    private final NotificationService notificationService;

    @Override
    public TradeOffer sendSpecificTradeOffer(SpecificTradeOfferRequest request) {
        Collector receiver = collectorService.findById(request.getReceiverId());
        Collector sender = collectorService.findById(request.getSenderId());

        TradeOffer tradeOffer = mapSpecificTradeOfferRequestToTradeOffer(request, sender, receiver);
        tradeOfferRepository.save(tradeOffer);

        Notification notification = new Notification();
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setRecipient(receiver);
        notification.setMessage("You have received a trade offer from " + sender.getUsername());

        notificationService.saveNotification(notification);

        return tradeOffer;
    }

    @Override
    public String sendGlobalTradeOffer(GlobalTradeOfferRequest request) {
        Collector sender = collectorService.findById(request.getSenderId());

        boolean senderHasOfferedSticker = sender.getOwnedStickers().containsKey(request.getOfferedStickerId());
        if (senderHasOfferedSticker) {
            List<Collector> collectors = collectorService.findPotentialTradingPartners(request.getOfferedStickerId(), request.getReceivedStickerId());

            for (Collector collector : collectors) {
                SpecificTradeOfferRequest specificTradeOfferRequest = new SpecificTradeOfferRequest();

                specificTradeOfferRequest.setSenderId(sender.getId());
                specificTradeOfferRequest.setReceiverId(collector.getId());
                specificTradeOfferRequest.setOfferedStickerId(request.getOfferedStickerId());
                specificTradeOfferRequest.setReceivedStickerId(request.getReceivedStickerId());
                specificTradeOfferRequest.setMessage(String.format("Hello, I would like to trade you my sticker %o: %s for your %o:%s",
                        request.getOfferedStickerId(), stickerService.findOne(request.getOfferedStickerId()).getName(),
                        request.getReceivedStickerId(), stickerService.findOne(request.getReceivedStickerId()).getName()));
                TradeOffer tradeOffer = mapSpecificTradeOfferRequestToTradeOffer(specificTradeOfferRequest, sender, collector);

                tradeOfferRepository.save(tradeOffer);

                Notification notification = new Notification();
                notification.setCreatedAt(LocalDateTime.now());
                notification.setRead(false);
                notification.setRecipient(collector);
                notification.setMessage("You have received a trade offer from " + sender.getUsername());

                notificationService.saveNotification(notification);
            }
        }

        return "Sent trade offers to potential partners";
    }

    @Override
    public List<TradeOffer> getPendingReceivedTradeOffers(Long collectorId) {
        return tradeOfferRepository.getPendingReceivedTradeOffersByUserId(collectorId);
    }

    @Override
    public List<TradeOffer> getPendingSentTradeOffers(Long collectorId) {
        return tradeOfferRepository.getPendingSentTradeOffersByUserId(collectorId);
    }

    @Override
    public TradeOffer acceptTradeOffer(AnswerPendingTradeOfferRequest request) throws Exception {
        TradeOffer tradeOffer = tradeOfferRepository.getTradeOfferByIds(request.getSenderId(), request.getReceiverId(), request.getOfferedStickerId(), request.getReceivedStickerId());

        Collector sender = collectorService.findById(request.getSenderId());
        Collector receiver = collectorService.findById(request.getReceiverId());

        sender.removeSticker(request.getOfferedStickerId());
        sender.addSticker(request.getReceivedStickerId());
        collectorService.save(sender);

        receiver.removeSticker(request.getReceivedStickerId());
        receiver.addSticker(request.getOfferedStickerId());
        collectorService.save(receiver);

        tradeOffer.setStatus("ACCEPTED");

        Notification notification = new Notification();
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setRecipient(sender);
        notification.setMessage(receiver.getUsername() + " accepted your trade offer");

        notificationService.saveNotification(notification);
        return tradeOfferRepository.save(tradeOffer);
    }

    @Override
    public TradeOffer refusePendingTradeOffer(AnswerPendingTradeOfferRequest request) {
        TradeOffer tradeOffer = tradeOfferRepository.getTradeOfferByIds(request.getSenderId(), request.getReceiverId(), request.getOfferedStickerId(), request.getReceivedStickerId());
        Collector sender = collectorService.findById(request.getSenderId());
        Collector receiver = collectorService.findById(request.getReceiverId());

        tradeOffer.setStatus("REFUSED");

        Notification notification = new Notification();
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setRecipient(sender);
        notification.setMessage(receiver.getUsername() + " refused your trade offer");

        notificationService.saveNotification(notification);

        return tradeOfferRepository.save(tradeOffer);
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
        tradeOffer.setStatus("PENDING");

        return tradeOffer;
    }
}
