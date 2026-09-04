package com.example.vib.client.dto;

import com.example.vib.client.Client;
import com.example.vib.client.ClientStatus;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String companyName,
        String contactName,
        String contactEmail,
        String industry,
        ClientStatus status) {

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getCompanyName(),
                client.getContactName(),
                client.getContactEmail(),
                client.getIndustry(),
                client.getStatus());
    }
}
