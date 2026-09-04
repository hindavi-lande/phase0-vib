package com.example.vib.campaign;

import com.example.vib.campaign.dto.CampaignRequest;
import com.example.vib.campaign.dto.CampaignResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignRequest request) {
        CampaignResponse created = campaignService.create(request);
        return ResponseEntity.created(URI.create("/api/campaigns/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public CampaignResponse get(@PathVariable UUID id) {
        return campaignService.get(id);
    }

    @GetMapping
    public List<CampaignResponse> list() {
        return campaignService.list();
    }

    @PutMapping("/{id}")
    public CampaignResponse update(@PathVariable UUID id, @Valid @RequestBody CampaignRequest request) {
        return campaignService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
