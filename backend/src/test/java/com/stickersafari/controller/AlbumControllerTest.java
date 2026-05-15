package com.stickersafari.controller;

import com.stickersafari.dto.AlbumCompletionResponse;
import com.stickersafari.dto.AlbumResponse;
import com.stickersafari.dto.StickerResponse;
import com.stickersafari.entity.Album;
import com.stickersafari.mapper.AlbumMapper;
import com.stickersafari.service.AlbumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumControllerTest {

    @Mock
    private AlbumService albumService;

    @Mock
    private AlbumMapper albumMapper;

    @InjectMocks
    private AlbumController albumController;

    private Album testAlbum;
    private AlbumResponse testAlbumResponse;
    private AlbumCompletionResponse testCompletionResponse;

    @BeforeEach
    void setUp() {
        testAlbum = new Album();
        testAlbum.setId(1L);
        testAlbum.setName("Test Album");

        testAlbumResponse = new AlbumResponse();
        testAlbumResponse.setId(1L);
        testAlbumResponse.setName("Test Album");
        testAlbumResponse.setStickers(new ArrayList<>());

        testCompletionResponse = new AlbumCompletionResponse();
        testCompletionResponse.setCompletion(75.0f);
        testCompletionResponse.setMissingStickerList(new ArrayList<>());
    }

    @Test
    void getAlbum_ShouldReturnAlbumResponse() {
        // Given
        when(albumService.getAlbum(1L)).thenReturn(testAlbum);
        when(albumMapper.toResponse(testAlbum)).thenReturn(testAlbumResponse);

        // When
        AlbumResponse result = albumController.getAlbum(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Album");
        verify(albumService).getAlbum(1L);
        verify(albumMapper).toResponse(testAlbum);
    }

    @Test
    void getAlbumCompletion_ShouldReturnCompletionResponse() {
        // Given
        when(albumService.getAlbumCompletion(1L, 1L)).thenReturn(testCompletionResponse);

        // When
        AlbumCompletionResponse result = albumController.getAlbumCompletion(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCompletion()).isEqualTo(75.0f);
        assertThat(result.getMissingStickerList()).isEmpty();
        verify(albumService).getAlbumCompletion(1L, 1L);
    }

    @Test
    void getAlbumCompletion_ShouldWorkWithDifferentCollectors() {
        // Given
        when(albumService.getAlbumCompletion(1L, 2L)).thenReturn(testCompletionResponse);

        // When
        AlbumCompletionResponse result = albumController.getAlbumCompletion(1L, 2L);

        // Then
        assertThat(result).isNotNull();
        verify(albumService).getAlbumCompletion(1L, 2L);
    }

    @Test
    void getAlbum_ShouldInvokeServiceOnce() {
        // Given
        when(albumService.getAlbum(1L)).thenReturn(testAlbum);
        when(albumMapper.toResponse(testAlbum)).thenReturn(testAlbumResponse);

        // When
        albumController.getAlbum(1L);

        // Then
        verify(albumService, times(1)).getAlbum(1L);
        verify(albumMapper, times(1)).toResponse(testAlbum);
    }
}
