/*
 * MIT License
 *
 * Copyright (c) 2020 Max Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package main.de.maxwell.qa.profile.domain;

import de.maxwell.qa.domain.profile.Profile;
import de.maxwell.qa.domain.profile.ProfileNotFoundException;
import de.maxwell.qa.domain.profile.ProfileRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import main.de.maxwell.qa.DatabaseResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@QuarkusTestResource(DatabaseResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProfileRepositoryIT {
    private static final Logger LOG = LoggerFactory.getLogger(ProfileRepositoryIT.class);

    @Inject
    ProfileRepository profileRepository;

    @BeforeAll
    public void setUp() {
        profileRepository.createProfile("1", "test","test");
        profileRepository.createProfile("2", "test","test");
        profileRepository.createProfile("3", "test","test");
        profileRepository.createProfile("4", "test","test");
        profileRepository.createProfile("5", "test","test");
        profileRepository.createProfile("6", "test","test");
        profileRepository.createProfile("7", "test","test");
    }

    @Test
    public void testFindById() {
        Profile profile = profileRepository.findById(1L);

        assertThat(profile.getUserID()).isEqualTo("1");
    }

    @Test
    public void testFindByIdNotFound() {
        assertThatThrownBy(() -> profileRepository.findById(99L)).isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Could not find profile with id 99");
    }

    @Test
    public void testListAllPaginated() {
        List<Profile> profiles = profileRepository.listAllPaginated(5, 0);

        assertThat(profiles.size()).isEqualTo(5);
    }

    @Test
    public void testUpdateFirstName() {
        Profile profile = profileRepository.updateFirstName(2L, "new first name");

        assertThat(profile.getFirstName()).isEqualTo("new first name");
    }

    @Test
    public void testUpdateFirstNameNotFound() {
        assertThatThrownBy(() -> profileRepository.updateFirstName(99L, "new first name")).isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Could not find profile with id 99");
    }

    @Test
    public void testUpdateLastName() {
        Profile profile = profileRepository.updateLastName(2L, "new last name");

        assertThat(profile.getLastName()).isEqualTo("new last name");
    }

    @Test
    public void testUpdateLastNameNotFound() {
        assertThatThrownBy(() -> profileRepository.updateLastName(99L, "new last name")).isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Could not find profile with id 99");
    }

    @Test
    public void testUpdateDescription() {
        Profile profile = profileRepository.updateDescription(2L, "new Description");

        assertThat(profile.getDescription()).isEqualTo("new Description");
    }

    @Test
    public void testUpdateDescriptionNotFound() {
        assertThatThrownBy(() -> profileRepository.updateDescription(99L, "new Description")).isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Could not find profile with id 99");
    }

    @Test
    public void testUpdateImage() {
        profileRepository.updateImage(2L, new Byte[0]);

        Profile profile = profileRepository.findById(2L);

        assertThat(profile.getUserID()).isEqualTo("2");
        assertThat(profile.getImage()).isEqualTo(new Byte[0]);
    }

    @Test
    public void testUpdateImageNotFound() {
        assertThatThrownBy(() -> profileRepository.updateImage(99L, new Byte[0])).isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Could not find profile with id 99");
    }

    @Test
    public void testUpdateReputation() {
        Long reputation = profileRepository.updateReputation(2L, 1);

        assertThat(reputation).isEqualTo(1);
    }

    @Test
    public void testUpdateReputationNotFound() {
        assertThatThrownBy(() -> profileRepository.updateReputation(99L, 1)).isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Could not find profile with id 99");
    }

    @Test
    @Order(1)
    public void testDelete() {
        profileRepository.removeProfile(7L);
        assertThatThrownBy(() -> profileRepository.findById(7L)).isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Could not find profile with id 7");
    }

    @Test
    public void testDeleteNotFound() {
        assertThatThrownBy(() -> profileRepository.removeProfile(99L)).isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining("Could not find profile with id 99");
    }
}
