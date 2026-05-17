package com.stickersafari.repository;

import com.stickersafari.entity.TradeOffer;
import com.stickersafari.entity.TradeOfferKey;
import com.stickersafari.entity.TradeOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeOfferRepository extends JpaRepository<TradeOffer, TradeOfferKey> {
    List<TradeOffer> findByReceiverIdAndStatus(Long collectorId, TradeOfferStatus status);

    List<TradeOffer> findBySenderIdAndStatus(Long collectorId, TradeOfferStatus status);

    @Query("SELECT t FROM TradeOffer t WHERE t.sender.id = ?1 AND t.receiver.id = ?2 AND t.offeredSticker.id = ?3 AND t.receivedSticker.id = ?4")
    TradeOffer findTradeOffer(Long senderId, Long receiverId, Long offeredStickerId, Long receivedStickerId);
}
