package com.stickersafari.service;

import com.stickersafari.dto.AlbumRequest;
import com.stickersafari.entity.Album;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.AlbumRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Album CRUD Operations Integration Tests")
class AlbumCrudIntegrationTest {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AlbumRepository albumRepository;

    @Test
    @DisplayName("Should create new album successfully")
    void testCreateAlbum() {
        // Given
        AlbumRequest request = new AlbumRequest();
        request.setName("Polar Animals");

        // When
        Album response = albumService.createAlbum(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Polar Animals");
        assertThat(response.getId()).isNotNull();

        Album savedAlbum = albumRepository.findById(response.getId()).orElseThrow();
        assertThat(savedAlbum.getName()).isEqualTo("Polar Animals");
    }

    @Test
    @DisplayName("Should retrieve all albums")
    void testGetAllAlbums() {
        // Given - Albums exist from seed data (African Safari, Ocean Life)

        // When
        List<Album> albums = albumService.getAllAlbums();

        // Then
        assertThat(albums).isNotEmpty();
        assertThat(albums)
                .extracting(Album::getName)
                .contains("African Safari", "Ocean Life");
    }

    @Test
    @DisplayName("Should update album name successfully")
    void testUpdateAlbum() {
        // Given
        AlbumRequest createRequest = new AlbumRequest();
        createRequest.setName("Original Name");
        Album created = albumService.createAlbum(createRequest);

        AlbumRequest updateRequest = new AlbumRequest();
        updateRequest.setName("Updated Name");

        // When
        Album updated = albumService.updateAlbum(created.getId(), updateRequest);

        // Then
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");

        Album savedAlbum = albumRepository.findById(created.getId()).orElseThrow();
        assertThat(savedAlbum.getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent album")
    void testUpdateNonExistentAlbum() {
        // Given
        AlbumRequest request = new AlbumRequest();
        request.setName("New Name");

        // When/Then
        assertThatThrownBy(() -> albumService.updateAlbum(99999L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Album not found");
    }

    @Test
    @DisplayName("Should delete album successfully")
    void testDeleteAlbum() {
        // Given
        AlbumRequest request = new AlbumRequest();
        request.setName("Album to Delete");
        Album created = albumService.createAlbum(request);

        // When
        albumService.deleteAlbum(created.getId());

        // Then
        assertThat(albumRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent album")
    void testDeleteNonExistentAlbum() {
        // When/Then
        assertThatThrownBy(() -> albumService.deleteAlbum(99999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Album not found");
    }

    @Test
    @DisplayName("Should get album by id")
    void testGetAlbumById() {
        // Given
        AlbumRequest request = new AlbumRequest();
        request.setName("Test Album");
        Album created = albumService.createAlbum(request);

        // When
        Album found = albumService.getAlbum(created.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Test Album");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent album")
    void testGetNonExistentAlbum() {
        // When/Then
        assertThatThrownBy(() -> albumService.getAlbum(99999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Album not found");
    }
}
