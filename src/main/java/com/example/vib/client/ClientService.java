package com.example.vib.client;

import com.example.vib.campaign.CampaignRepository;
import com.example.vib.client.dto.ClientRequest;
import com.example.vib.client.dto.ClientResponse;
import com.example.vib.common.DuplicateResourceException;
import com.example.vib.common.ResourceInUseException;
import com.example.vib.common.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;
    private final CampaignRepository campaignRepository;

    public ClientService(ClientRepository clientRepository, CampaignRepository campaignRepository) {
        this.clientRepository = clientRepository;
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        if (clientRepository.existsByContactEmailIgnoreCase(request.contactEmail())) {
            throw new DuplicateResourceException("Client already exists with contactEmail: " + request.contactEmail());
        }

        Client client = new Client(
                request.companyName(),
                request.contactName(),
                request.contactEmail(),
                request.industry(),
                request.status());

        return ClientResponse.from(clientRepository.save(client));
    }

    public ClientResponse get(UUID id) {
        return ClientResponse.from(findOrThrow(id));
    }

    public List<ClientResponse> list() {
        return clientRepository.findAll().stream()
                .map(ClientResponse::from)
                .toList();
    }

    @Transactional
    public ClientResponse update(UUID id, ClientRequest request) {
        Client client = findOrThrow(id);

        if (clientRepository.existsByContactEmailIgnoreCaseAndIdNot(request.contactEmail(), id)) {
            throw new DuplicateResourceException("Client already exists with contactEmail: " + request.contactEmail());
        }

        client.setCompanyName(request.companyName());
        client.setContactName(request.contactName());
        client.setContactEmail(request.contactEmail());
        client.setIndustry(request.industry());
        client.setStatus(request.status());

        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(UUID id) {
        Client client = findOrThrow(id);

        // Reject rather than let the campaigns.client_id constraint surface as a 500.
        if (campaignRepository.existsByClientId(id)) {
            throw new ResourceInUseException("Client cannot be deleted while campaigns still reference it: " + id);
        }

        clientRepository.delete(client);
    }

    /** Shared lookup so the Campaign slice can resolve the FK without duplicating the 404. */
    public Client findOrThrow(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }
}
