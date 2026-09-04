package com.example.vib.campaign;

import com.example.vib.campaign.dto.CampaignRequest;
import com.example.vib.campaign.dto.CampaignResponse;
import com.example.vib.client.Client;
import com.example.vib.client.ClientService;
import com.example.vib.common.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final ClientService clientService;

    public CampaignService(CampaignRepository campaignRepository, ClientService clientService) {
        this.campaignRepository = campaignRepository;
        this.clientService = clientService;
    }

    @Transactional
    public CampaignResponse create(CampaignRequest request) {
        Client client = clientService.findOrThrow(request.clientId());

        Campaign campaign = new Campaign(client, request.name(), request.targetCriteria(), request.status());

        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    public CampaignResponse get(UUID id) {
        return CampaignResponse.from(findOrThrow(id));
    }

    public List<CampaignResponse> list() {
        return campaignRepository.findAll().stream()
                .map(CampaignResponse::from)
                .toList();
    }

    @Transactional
    public CampaignResponse update(UUID id, CampaignRequest request) {
        Campaign campaign = findOrThrow(id);

        // Re-resolve the FK so a campaign can be moved to a different client.
        campaign.setClient(clientService.findOrThrow(request.clientId()));
        campaign.setName(request.name());
        campaign.setTargetCriteria(request.targetCriteria());
        campaign.setStatus(request.status());

        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    @Transactional
    public void delete(UUID id) {
        campaignRepository.delete(findOrThrow(id));
    }

    private Campaign findOrThrow(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", id));
    }
}
