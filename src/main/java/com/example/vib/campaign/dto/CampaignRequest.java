package com.example.vib.campaign.dto;

import com.example.vib.campaign.CampaignStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CampaignRequest(
        @NotNull(message = "clientId is required")
        UUID clientId,

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name,

        @NotBlank(message = "targetCriteria is required")
        @Size(max = 500, message = "targetCriteria must be at most 500 characters")
        String targetCriteria,

        @NotNull(message = "status is required")
        CampaignStatus status) {
}
