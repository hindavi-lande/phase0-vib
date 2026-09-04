package com.example.vib.campaign.dto;

import com.example.vib.campaign.Campaign;
import com.example.vib.campaign.CampaignStatus;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        UUID clientId,
        String name,
        String targetCriteria,
        CampaignStatus status) {

    public static CampaignResponse from(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getClient().getId(),
                campaign.getName(),
                campaign.getTargetCriteria(),
                campaign.getStatus());
    }
}
