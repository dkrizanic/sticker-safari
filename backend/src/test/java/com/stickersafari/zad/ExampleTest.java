package com.stickersafari.zad;

import com.stickersafari.dto.*;
import com.stickersafari.repository.*;
import com.stickersafari.entity.Collector;
import com.stickersafari.entity.TradeOffer;
import com.stickersafari.exception.StickerNotOwnedException;
import com.stickersafari.service.TradingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Sql({"/sql/insertData.sql"})
public class ExampleTest {

    @Autowired
    private TradingService tradingService;

    @Autowired
    private CollectorRepository collectorRepository;
    @Autowired
    private StickerRepository stickerRepository;
    @Autowired
    private TradeOfferRepository tradeOfferRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @AfterEach
    void afterEach() {
        notificationRepository.deleteAll();
        tradeOfferRepository.deleteAll();
        collectorRepository.deleteAll();
        albumRepository.deleteAll();
        stickerRepository.deleteAll();
    }

    @Test
    public void shouldChangeNothingWhenAcceptTradeThrowsException() {
        AnswerPendingTradeOfferRequest request = new AnswerPendingTradeOfferRequest();

        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setOfferedStickerId(1L);
        request.setReceivedStickerId(2L);
        Assertions.assertThrows(StickerNotOwnedException.class, () -> tradingService.acceptTradeOffer(request));

        entityManager.clear();

        Collector sender = collectorRepository.findById(1L).get();
        Collector receiver = collectorRepository.findById(2L).get();

        Integer senderReceivedStickerQuantity = sender.getOwnedStickers().get(2L);
        Integer senderSentStickerQuantity = sender.getOwnedStickers().get(1L);
        Integer receiverReceivedStickerQuantity = receiver.getOwnedStickers().get(1L);
        Integer receiverSentStickerQuantity = receiver.getOwnedStickers().get(2L);

        Assertions.assertNull(senderReceivedStickerQuantity);
        Assertions.assertEquals(1, senderSentStickerQuantity);
        Assertions.assertNull(receiverReceivedStickerQuantity);
        Assertions.assertNull(receiverSentStickerQuantity);
    }

    @Test
    public void shouldReturnAlbumCompletionWhenGetAlbumCompletion() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/album/1/collector/1"))
                .andExpect(status().isOk()).andReturn();

        AlbumCompletionResponse albumCompletionResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AlbumCompletionResponse.class);

        Assertions.assertEquals(33.34f, albumCompletionResponse.getCompletion());
        Assertions.assertEquals(2, albumCompletionResponse.getMissingStickerList().size());
    }

    @Test
    public void shouldReturnNotFoundWhenGetAlbumCompletionAndAlbumNotExists() throws Exception {
        mockMvc.perform(get("/album/99/collector/1"))
                .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    public void shouldReturn5OpenedStickersWhenOpenPackWithSize5() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/stickers/open/album/1/collector/1?size=5"))
                .andExpect(status().isOk())
                .andReturn();

        List<StickerResponse> stickerResponses = Arrays.asList(objectMapper.readValue(mvcResult.getResponse().getContentAsString(), StickerResponse[].class));

        Assertions.assertEquals(5, stickerResponses.size());
    }

    @Test
    public void shouldNotSaveTradeOfferWhenCollectorsDontHaveTheStickers() {
        SpecificTradeOfferRequest request = new SpecificTradeOfferRequest();
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setOfferedStickerId(1L);
        request.setReceivedStickerId(3L);
        request.setMessage("Hello");

        Assertions.assertThrows(StickerNotOwnedException.class, () -> tradingService.sendSpecificTradeOffer(request));

        TradeOffer savedTradeOffer = tradeOfferRepository.getTradeOfferByIds(1L, 2L, 1L, 3L);
        Assertions.assertNull(savedTradeOffer);
    }

    @Test
    public void shouldSendGlobalTradeOfferWhenConditionsAreMet() {
        GlobalTradeOfferRequest request = new GlobalTradeOfferRequest();
        request.setSenderId(1L);
        request.setOfferedStickerId(1L);
        request.setReceivedStickerId(3L);

        tradingService.sendGlobalTradeOffer(request);

        TradeOffer savedTradeOfferForCollector3 = tradeOfferRepository.getTradeOfferByIds(1L, 3L, 1L, 3L);
        TradeOffer savedTradeOfferForCollector2 = tradeOfferRepository.getTradeOfferByIds(1L, 2L, 1L, 3L);
        Assertions.assertNotNull(savedTradeOfferForCollector3);
        Assertions.assertNull(savedTradeOfferForCollector2);
    }

}
