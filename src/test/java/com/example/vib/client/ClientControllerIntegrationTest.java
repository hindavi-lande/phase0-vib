package com.example.vib.client;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.vib.campaign.CampaignRepository;
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
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @BeforeEach
    void clean() {
        campaignRepository.deleteAll();
        clientRepository.deleteAll();
    }

    private static String clientJson(String contactEmail) {
        return """
                {
                  "companyName": "Acme Security",
                  "contactName": "Priya Shah",
                  "contactEmail": "%s",
                  "industry": "Cybersecurity",
                  "status": "ACTIVE"
                }
                """.formatted(contactEmail);
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("priya@acmesecurity.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.companyName").value("Acme Security"))
                .andExpect(jsonPath("$.contactEmail").value("priya@acmesecurity.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = body.get("id").asText();

        mockMvc.perform(get("/api/clients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.contactName").value("Priya Shah"));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(1)));

        mockMvc.perform(put("/api/clients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Cloudify",
                                  "contactName": "Sam Lee",
                                  "contactEmail": "sam@cloudify.io",
                                  "industry": "Cloud/DevOps",
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Cloudify"))
                .andExpect(jsonPath("$.contactEmail").value("sam@cloudify.io"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(delete("/api/clients/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/clients/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void getUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/clients/{id}", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Client not found")));
    }

    @Test
    void invalidPayloadReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "",
                                  "contactName": "Priya Shah",
                                  "contactEmail": "not-an-email",
                                  "industry": "Cybersecurity",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.companyName").exists())
                .andExpect(jsonPath("$.fieldErrors.contactEmail").exists());
    }

    @Test
    void duplicateContactEmailReturns409() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("dup@acmesecurity.com")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("dup@acmesecurity.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
