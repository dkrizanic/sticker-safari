package com.stickersafari.service;

import com.stickersafari.entity.Album;
import com.stickersafari.entity.Sticker;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.StickerRepository;
import com.stickersafari.service.impl.StickerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StickerServiceImplTest {

    @Mock
    private StickerRepository stickerRepository;

    @InjectMocks
    private StickerServiceImpl stickerService;

    private Album testAlbum;
    private List<Sticker> testStickers;

    @BeforeEach
    void setUp() {
        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setName("Test Album");

        testStickers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Sticker sticker = new Sticker();
            sticker.setId((long) i);
            sticker.setName("Sticker " + i);
            sticker.setAlbum(testAlbum);
            testStickers.add(sticker);
        }
    }

    @Test
    void findOne_ShouldReturnSticker_WhenStickerExists() {
        // Given
        Sticker testSticker = testStickers.get(0);
        when(stickerRepository.findById(1L)).thenReturn(Optional.of(testSticker));

        // When
        Sticker result = stickerService.findOne(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Sticker 1");
        verify(stickerRepository).findById(1L);
    }

    @Test
    void findOne_ShouldThrowException_WhenStickerDoesNotExist() {
        // Given
        when(stickerRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stickerService.findOne(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sticker not found with ID: 99");
        verify(stickerRepository).findById(99L);
    }

    @Test
    void getStickersOfAlbum_ShouldReturnAllStickers() {
        // Given
        when(stickerRepository.findByAlbumId(1L)).thenReturn(testStickers);

        // When
        List<Sticker> result = stickerService.getStickersOfAlbum(1L);

        // Then
        assertThat(result).hasSize(10);
        assertThat(result).isEqualTo(testStickers);
        verify(stickerRepository).findByAlbumId(1L);
    }

    @Test
    void openPack_ShouldReturnCorrectNumberOfStickers() {
        // Given
        when(stickerRepository.findByAlbumId(1L)).thenReturn(testStickers);

        // When
        List<Sticker> result = stickerService.openPack(1L, 1L, 5);

        // Then
        assertThat(result).hasSize(5);
        verify(stickerRepository).findByAlbumId(1L);
    }

    @Test
    void openPack_ShouldThrowException_WhenSizeTooSmall() {
        // When & Then
        assertThatThrownBy(() -> stickerService.openPack(1L, 1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pack size must be between 1 and 10");
        verify(stickerRepository, never()).findByAlbumId(anyLong());
    }

    @Test
    void openPack_ShouldThrowException_WhenSizeTooLarge() {
        // When & Then
        assertThatThrownBy(() -> stickerService.openPack(1L, 1L, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pack size must be between 1 and 10");
        verify(stickerRepository, never()).findByAlbumId(anyLong());
    }

    @Test
    void openPack_ShouldThrowException_WhenNoStickersInAlbum() {
        // Given
        when(stickerRepository.findByAlbumId(1L)).thenReturn(new ArrayList<>());

        // When & Then
        assertThatThrownBy(() -> stickerService.openPack(1L, 1L, 3))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No stickers found in album: 1");
        verify(stickerRepository).findByAlbumId(1L);
    }

    @Test
    void openPack_ShouldReturnStickersFromAlbum() {
        // Given
        when(stickerRepository.findByAlbumId(1L)).thenReturn(testStickers);

        // When
        List<Sticker> result = stickerService.openPack(1L, 1L, 3);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(sticker -> testStickers.contains(sticker));
        verify(stickerRepository).findByAlbumId(1L);
    }

    @Test
    void openPack_ShouldWorkWithMinimumPackSize() {
        // Given
        when(stickerRepository.findByAlbumId(1L)).thenReturn(testStickers);

        // When
        List<Sticker> result = stickerService.openPack(1L, 1L, 1);

        // Then
        assertThat(result).hasSize(1);
        verify(stickerRepository).findByAlbumId(1L);
    }

    @Test
    void openPack_ShouldWorkWithMaximumPackSize() {
        // Given
        when(stickerRepository.findByAlbumId(1L)).thenReturn(testStickers);

        // When
        List<Sticker> result = stickerService.openPack(1L, 1L, 10);

        // Then
        assertThat(result).hasSize(10);
        verify(stickerRepository).findByAlbumId(1L);
    }
}
