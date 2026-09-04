package com.example.vib.campaign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.vib.campaign.dto.CampaignRequest;
import com.example.vib.campaign.dto.CampaignResponse;
import com.example.vib.client.Client;
import com.example.vib.client.ClientService;
import com.example.vib.client.ClientStatus;
import com.example.vib.common.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private CampaignService campaignService;

    private UUID clientId;
    private Client client;
    private CampaignRequest request;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        client = new Client("Acme Security", "Priya Shah", "priya@acmesecurity.com", "Cybersecurity", ClientStatus.ACTIVE);
        request = new CampaignRequest(
                clientId,
                "Q4 IT Security Decision-Makers",
                "IT security decision-makers at mid-size companies",
                CampaignStatus.DRAFT);
    }

    @Test
    void createResolvesForeignKeyAndPersists() {
        when(clientService.findOrThrow(clientId)).thenReturn(client);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));

        CampaignResponse response = campaignService.create(request);

        assertThat(response.name()).isEqualTo("Q4 IT Security Decision-Makers");
        assertThat(response.targetCriteria()).isEqualTo("IT security decision-makers at mid-size companies");
        assertThat(response.status()).isEqualTo(CampaignStatus.DRAFT);
        verify(clientService).findOrThrow(clientId);
    }

    @Test
    void createFailsWhenClientMissing() {
        when(clientService.findOrThrow(clientId)).thenThrow(new ResourceNotFoundException("Client", clientId));

        assertThatThrownBy(() -> campaignService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client");

        verify(campaignRepository, never()).save(any());
    }

    @Test
    void getThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(campaignRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.get(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Campaign");
    }

    @Test
    void updateReassignsClientAndFields() {
        UUID id = UUID.randomUUID();
        UUID newClientId = UUID.randomUUID();
        Client newClient = new Client("Cloudify", "Sam Lee", "sam@cloudify.io", "Cloud/DevOps", ClientStatus.ACTIVE);

        Campaign existing = new Campaign(
                client, "Q4 IT Security Decision-Makers", "IT security decision-makers", CampaignStatus.DRAFT);

        when(campaignRepository.findById(id)).thenReturn(Optional.of(existing));
        when(clientService.findOrThrow(newClientId)).thenReturn(newClient);
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));

        CampaignResponse response = campaignService.update(
                id,
                new CampaignRequest(
                        newClientId,
                        "Cloud/DevOps Buyers",
                        "Cloud and DevOps engineers at enterprise companies",
                        CampaignStatus.ACTIVE));

        assertThat(response.name()).isEqualTo("Cloud/DevOps Buyers");
        assertThat(response.targetCriteria()).isEqualTo("Cloud and DevOps engineers at enterprise companies");
        assertThat(response.status()).isEqualTo(CampaignStatus.ACTIVE);
        assertThat(existing.getClient()).isSameAs(newClient);
    }

    @Test
    void deleteRemovesCampaign() {
        UUID id = UUID.randomUUID();
        Campaign existing = new Campaign(
                client, "Q4 IT Security Decision-Makers", "IT security decision-makers", CampaignStatus.DRAFT);
        when(campaignRepository.findById(id)).thenReturn(Optional.of(existing));

        campaignService.delete(id);

        verify(campaignRepository).delete(existing);
    }
}
