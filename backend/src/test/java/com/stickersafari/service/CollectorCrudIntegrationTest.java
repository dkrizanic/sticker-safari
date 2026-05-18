package com.stickersafari.service;

import com.stickersafari.dto.CollectorRequest;
import com.stickersafari.entity.Collector;
import com.stickersafari.exception.EntityNotFoundException;
import com.stickersafari.repository.CollectorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Collector CRUD Operations Integration Tests")
class CollectorCrudIntegrationTest {

    @Autowired
    private CollectorService collectorService;

    @Autowired
    private CollectorRepository collectorRepository;

    /**
     * Creates a mock Authentication object for testing.
     * @param username The username
     * @param isAdmin Whether the user has admin role
     * @return Authentication object
     */
    private Authentication createAuthentication(String username, boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = isAdmin
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    @Test
    @DisplayName("Should create new collector successfully")
    void testCreateCollector() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("NewUser");

        // When
        Collector response = collectorService.createCollector(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("NewUser");
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStars()).isEqualTo(0);
        assertThat(response.getDailyStickerCount()).isEqualTo(0);

        Collector saved = collectorRepository.findById(response.getId()).orElseThrow();
        assertThat(saved.getUsername()).isEqualTo("NewUser");
    }

    @Test
    @DisplayName("Should retrieve all collectors")
    void testGetAllCollectors() {
        // Given - Create test collectors
        CollectorRequest request1 = new CollectorRequest();
        request1.setUsername("TestUser1");
        collectorService.createCollector(request1);

        CollectorRequest request2 = new CollectorRequest();
        request2.setUsername("TestUser2");
        collectorService.createCollector(request2);

        // When
        List<Collector> collectors = collectorService.getAllCollectors();

        // Then
        assertThat(collectors).hasSizeGreaterThanOrEqualTo(2);
        assertThat(collectors)
                .extracting(Collector::getUsername)
                .contains("TestUser1", "TestUser2");
    }

    @Test
    @DisplayName("Should find collector by username")
    void testFindByUsername() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("UniqueUser");
        collectorService.createCollector(request);

        // When
        Collector found = collectorService.findByUsername("UniqueUser");

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("UniqueUser");
    }

    @Test
    @DisplayName("Should update collector username")
    void testUpdateCollector() {
        // Given
        CollectorRequest createRequest = new CollectorRequest();
        createRequest.setUsername("OldUsername");
        Collector created = collectorService.createCollector(createRequest);

        CollectorRequest updateRequest = new CollectorRequest();
        updateRequest.setUsername("NewUsername");

        // When - Update as the owner
        Authentication auth = createAuthentication("OldUsername", false);
        Collector updated = collectorService.updateCollector(created.getId(), updateRequest, auth);

        // Then
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getUsername()).isEqualTo("NewUsername");

        Collector saved = collectorRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getUsername()).isEqualTo("NewUsername");
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent collector")
    void testUpdateNonExistentCollector() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("NewName");

        // When/Then - Update as admin
        Authentication auth = createAuthentication("admin", true);
        assertThatThrownBy(() -> collectorService.updateCollector(99999L, request, auth))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Collector not found");
    }

    @Test
    @DisplayName("Should delete collector successfully")
    void testDeleteCollector() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("UserToDelete");
        Collector created = collectorService.createCollector(request);

        // When - Delete as the owner
        Authentication auth = createAuthentication("UserToDelete", false);
        collectorService.deleteCollector(created.getId(), auth);

        // Then
        assertThat(collectorRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent collector")
    void testDeleteNonExistentCollector() {
        // When/Then - Delete as admin
        Authentication auth = createAuthentication("admin", true);
        assertThatThrownBy(() -> collectorService.deleteCollector(99999L, auth))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Collector not found");
    }

    @Test
    @DisplayName("Should get collector by id")
    void testGetCollectorById() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("TestUser");
        Collector created = collectorService.createCollector(request);

        // When
        Collector found = collectorService.findById(created.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getUsername()).isEqualTo("TestUser");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent collector")
    void testGetNonExistentCollector() {
        // When/Then
        assertThatThrownBy(() -> collectorService.findById(99999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Collector not found");
    }

    @Test
    @DisplayName("Should initialize new collector with default values")
    void testCollectorDefaultValues() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("DefaultUser");

        // When
        Collector response = collectorService.createCollector(request);

        // Then
        assertThat(response.getStars()).isEqualTo(0);
        assertThat(response.getDailyStickerCount()).isEqualTo(0);
        assertThat(response.getLastPackOpenDate()).isNull();
        assertThat(response.getOwnedStickers()).isEmpty();
    }

    @Test
    @DisplayName("Should allow user to update their own profile")
    void testUserCanUpdateOwnProfile() {
        // Given
        CollectorRequest createRequest = new CollectorRequest();
        createRequest.setUsername("OwnerUser");
        Collector created = collectorService.createCollector(createRequest);

        CollectorRequest updateRequest = new CollectorRequest();
        updateRequest.setUsername("UpdatedOwnerUser");

        // When - User updates their own profile
        Authentication auth = createAuthentication("OwnerUser", false);
        Collector updated = collectorService.updateCollector(created.getId(), updateRequest, auth);

        // Then
        assertThat(updated.getUsername()).isEqualTo("UpdatedOwnerUser");
    }

    @Test
    @DisplayName("Should prevent user from updating another user's profile")
    void testUserCannotUpdateOtherProfile() {
        // Given
        CollectorRequest createRequest = new CollectorRequest();
        createRequest.setUsername("VictimUser");
        Collector created = collectorService.createCollector(createRequest);

        CollectorRequest updateRequest = new CollectorRequest();
        updateRequest.setUsername("HackedUser");

        // When/Then - Different user tries to update
        Authentication auth = createAuthentication("AttackerUser", false);
        assertThatThrownBy(() -> collectorService.updateCollector(created.getId(), updateRequest, auth))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
    }

    @Test
    @DisplayName("Should allow admin to update any user's profile")
    void testAdminCanUpdateAnyProfile() {
        // Given
        CollectorRequest createRequest = new CollectorRequest();
        createRequest.setUsername("RegularUser");
        Collector created = collectorService.createCollector(createRequest);

        CollectorRequest updateRequest = new CollectorRequest();
        updateRequest.setUsername("AdminUpdatedUser");

        // When - Admin updates another user's profile
        Authentication auth = createAuthentication("admin", true);
        Collector updated = collectorService.updateCollector(created.getId(), updateRequest, auth);

        // Then
        assertThat(updated.getUsername()).isEqualTo("AdminUpdatedUser");
    }

    @Test
    @DisplayName("Should allow user to delete their own profile")
    void testUserCanDeleteOwnProfile() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("ToDeleteOwn");
        Collector created = collectorService.createCollector(request);

        // When - User deletes their own profile
        Authentication auth = createAuthentication("ToDeleteOwn", false);
        collectorService.deleteCollector(created.getId(), auth);

        // Then
        assertThat(collectorRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should prevent user from deleting another user's profile")
    void testUserCannotDeleteOtherProfile() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("ProtectedUser");
        Collector created = collectorService.createCollector(request);

        // When/Then - Different user tries to delete
        Authentication auth = createAuthentication("AttackerUser", false);
        assertThatThrownBy(() -> collectorService.deleteCollector(created.getId(), auth))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("You can only delete your own profile");
    }

    @Test
    @DisplayName("Should allow admin to delete any user's profile")
    void testAdminCanDeleteAnyProfile() {
        // Given
        CollectorRequest request = new CollectorRequest();
        request.setUsername("ToDeleteByAdmin");
        Collector created = collectorService.createCollector(request);

        // When - Admin deletes another user's profile
        Authentication auth = createAuthentication("admin", true);
        collectorService.deleteCollector(created.getId(), auth);

        // Then
        assertThat(collectorRepository.findById(created.getId())).isEmpty();
    }
}
