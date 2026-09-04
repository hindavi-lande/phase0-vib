package com.example.vib.campaign;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vib.client.ClientRepository;
import com.example.vib.client.ClientStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CampaignControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ClientRepository clientRepository;

    private String clientId;

    @BeforeEach
    void setUp() throws Exception {
        campaignRepository.deleteAll();
        clientRepository.deleteAll();

        MvcResult created = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Acme Security",
                                  "contactName": "Priya Shah",
                                  "contactEmail": "priya@acmesecurity.com",
                                  "industry": "Cybersecurity",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        clientId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
    }

    private String campaignJson(String name) {
        return """
                {
                  "clientId": "%s",
                  "name": "%s",
                  "targetCriteria": "IT security decision-makers at mid-size companies",
                  "status": "DRAFT"
                }
                """.formatted(clientId, name);
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(campaignJson("Q4 IT Security Decision-Makers")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.name").value("Q4 IT Security Decision-Makers"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = body.get("id").asText();

        mockMvc.perform(get("/api/campaigns/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.targetCriteria").value("IT security decision-makers at mid-size companies"));

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(1)));

        mockMvc.perform(put("/api/campaigns/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "%s",
                                  "name": "Q1 Renewal Push",
                                  "targetCriteria": "Existing customers up for renewal",
                                  "status": "ACTIVE"
                                }
                                """.formatted(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Q1 Renewal Push"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(delete("/api/campaigns/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/campaigns/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/campaigns/{id}", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Campaign not found")));
    }

    @Test
    void createWithUnknownClientIdReturns404() throws Exception {
        mockMvc.perform(post("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "11111111-1111-1111-1111-111111111111",
                                  "name": "Q4 IT Security Decision-Makers",
                                  "targetCriteria": "IT security decision-makers at mid-size companies",
                                  "status": "DRAFT"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Client not found")));
    }

    @Test
    void invalidPayloadReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "%s",
                                  "name": "",
                                  "targetCriteria": "",
                                  "status": "DRAFT"
                                }
                                """.formatted(clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.targetCriteria").exists());
    }

    @Test
    void clientCannotBeDeletedWhileCampaignReferencesIt() throws Exception {
        mockMvc.perform(post("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(campaignJson("Q4 IT Security Decision-Makers")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/clients/{id}", clientId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
