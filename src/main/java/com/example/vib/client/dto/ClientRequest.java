package com.example.vib.client.dto;

import com.example.vib.client.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClientRequest(
        @NotBlank(message = "companyName is required")
        @Size(max = 150, message = "companyName must be at most 150 characters")
        String companyName,

        @NotBlank(message = "contactName is required")
        @Size(max = 100, message = "contactName must be at most 100 characters")
        String contactName,

        @NotBlank(message = "contactEmail is required")
        @Email(message = "contactEmail must be a valid address")
        @Size(max = 254, message = "contactEmail must be at most 254 characters")
        String contactEmail,

        @NotBlank(message = "industry is required")
        @Size(max = 100, message = "industry must be at most 100 characters")
        String industry,

        @NotNull(message = "status is required")
        ClientStatus status) {
}
