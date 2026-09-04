package com.example.vib.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.vib.campaign.CampaignRepository;
import com.example.vib.client.dto.ClientRequest;
import com.example.vib.client.dto.ClientResponse;
import com.example.vib.common.DuplicateResourceException;
import com.example.vib.common.ResourceInUseException;
import com.example.vib.common.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientRequest request;

    @BeforeEach
    void setUp() {
        request = new ClientRequest(
                "Acme Security", "Priya Shah", "priya@acmesecurity.com", "Cybersecurity", ClientStatus.ACTIVE);
    }

    @Test
    void createPersistsClient() {
        when(clientRepository.existsByContactEmailIgnoreCase("priya@acmesecurity.com")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientResponse response = clientService.create(request);

        assertThat(response.companyName()).isEqualTo("Acme Security");
        assertThat(response.contactEmail()).isEqualTo("priya@acmesecurity.com");
        assertThat(response.status()).isEqualTo(ClientStatus.ACTIVE);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void createRejectsDuplicateContactEmail() {
        when(clientRepository.existsByContactEmailIgnoreCase("priya@acmesecurity.com")).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("priya@acmesecurity.com");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void getThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.get(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void listMapsEveryClient() {
        when(clientRepository.findAll()).thenReturn(List.of(
                new Client("Acme Security", "Priya Shah", "priya@acmesecurity.com", "Cybersecurity", ClientStatus.ACTIVE),
                new Client("Cloudify", "Sam Lee", "sam@cloudify.io", "Cloud/DevOps", ClientStatus.INACTIVE)));

        List<ClientResponse> clients = clientService.list();

        assertThat(clients).hasSize(2);
        assertThat(clients).extracting(ClientResponse::companyName).containsExactly("Acme Security", "Cloudify");
    }

    @Test
    void updateAppliesEveryField() {
        UUID id = UUID.randomUUID();
        Client existing = new Client(
                "Acme Security", "Priya Shah", "priya@acmesecurity.com", "Cybersecurity", ClientStatus.ACTIVE);
        when(clientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(clientRepository.existsByContactEmailIgnoreCaseAndIdNot("sam@cloudify.io", id)).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientResponse response = clientService.update(
                id,
                new ClientRequest("Cloudify", "Sam Lee", "sam@cloudify.io", "Cloud/DevOps", ClientStatus.INACTIVE));

        assertThat(response.companyName()).isEqualTo("Cloudify");
        assertThat(response.contactName()).isEqualTo("Sam Lee");
        assertThat(response.contactEmail()).isEqualTo("sam@cloudify.io");
        assertThat(response.industry()).isEqualTo("Cloud/DevOps");
        assertThat(response.status()).isEqualTo(ClientStatus.INACTIVE);
    }

    @Test
    void deleteRemovesClientWithoutCampaigns() {
        UUID id = UUID.randomUUID();
        Client existing = new Client(
                "Acme Security", "Priya Shah", "priya@acmesecurity.com", "Cybersecurity", ClientStatus.ACTIVE);
        when(clientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(campaignRepository.existsByClientId(id)).thenReturn(false);

        clientService.delete(id);

        verify(clientRepository).delete(existing);
    }

    @Test
    void deleteRejectsClientWithCampaigns() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id))
                .thenReturn(Optional.of(new Client(
                        "Acme Security", "Priya Shah", "priya@acmesecurity.com", "Cybersecurity", ClientStatus.ACTIVE)));
        when(campaignRepository.existsByClientId(id)).thenReturn(true);

        assertThatThrownBy(() -> clientService.delete(id))
                .isInstanceOf(ResourceInUseException.class);

        verify(clientRepository, never()).delete(any());
    }
}
