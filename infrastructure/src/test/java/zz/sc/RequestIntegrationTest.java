package zz.sc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import zz.sc.infrastructure.persistence.entity.IdempotencyRecordEntity;
import zz.sc.infrastructure.persistence.repository.JpaIdempotencyRecordRepository;
import zz.sc.infrastructure.rest.dto.CreateRequestDto;
import zz.sc.infrastructure.rest.dto.ReasonDto;
import zz.sc.infrastructure.rest.dto.RequestResponseDto;
import zz.sc.infrastructure.rest.dto.UpdateRequestDto;
import zz.sc.model.ActionReason;
import zz.sc.model.RequestState;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestConfig.class)
@AutoConfigureMockMvc
@Testcontainers
class RequestIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private JpaIdempotencyRecordRepository idempotencyRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFollowHappyPath() throws Exception {
        // 1. Create Application
        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setName("Test Req");
        createDto.setContent("Initial Content");

        MvcResult createResult = mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Req"))
                .andExpect(jsonPath("$.content").value("Initial Content"))
                .andExpect(jsonPath("$.state").value(RequestState.CREATED.name()))
                .andReturn();

        RequestResponseDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), RequestResponseDto.class);
        UUID id = created.getId();

        // 2. Edit content
        UpdateRequestDto updateDto = new UpdateRequestDto();
        updateDto.setContent("Updated Content");

        mockMvc.perform(put("/api/requests/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.content").value("Updated Content"));

        // 3. Verify
        mockMvc.perform(post("/api/requests/" + id + "/verify"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.state").value(RequestState.VERIFIED.name()));

        // 4. Accept
        mockMvc.perform(post("/api/requests/" + id + "/accept"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.state").value(RequestState.ACCEPTED.name()));

        // 5. Publish
        mockMvc.perform(post("/api/requests/" + id + "/publish"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.state").value(RequestState.PUBLISHED.name()))
                .andExpect(jsonPath("$.sequenceNumber").isNumber());
    }

    @Test
    void shouldRejectRequestWithReason() throws Exception {
        // 1. Create
        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setName("Test Reject");
        createDto.setContent("Content");
        MvcResult createResult = mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();
        RequestResponseDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), RequestResponseDto.class);
        UUID id = created.getId();

        // 2. Verify
        mockMvc.perform(post("/api/requests/" + id + "/verify"))
                .andExpect(status().isOk());


        // 3. Reject
        ReasonDto reasonDto = new ReasonDto();
        reasonDto.setReason(ActionReason.OTHER);

        mockMvc.perform(post("/api/requests/" + id + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reasonDto)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.state").value(RequestState.REJECTED.name()))
                .andExpect(jsonPath("$.actionReason").value(ActionReason.OTHER.name()));
    }

    @Test
    void shouldFailValidationOnCreate() throws Exception {
        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setContent("Content");

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void shouldFailInvalidStateTransition() throws Exception {
        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setName("Test Error");
        createDto.setContent("Content");
        MvcResult createResult = mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn();
        RequestResponseDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(), RequestResponseDto.class);

        // Try to Accept directly from CREATED (invalid)
        mockMvc.perform(post("/api/requests/" + created.getId() + "/accept"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").exists());
                
        // Try to Publish directly from CREATED (invalid)
        mockMvc.perform(post("/api/requests/" + created.getId() + "/publish"))
                .andExpect(status().isConflict());
                
        // Now Verify it (valid)
        mockMvc.perform(post("/api/requests/" + created.getId() + "/verify"))
                .andExpect(status().isOk());

                
        // Try to Verify again (invalid)
        mockMvc.perform(post("/api/requests/" + created.getId() + "/verify"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldHandleIdempotencyKey() throws Exception {
        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setName("Idempotency Test");
        createDto.setContent("Initial Content");

        UUID idempotencyKey = UUID.randomUUID();

        MvcResult firstResult = mockMvc.perform(post("/api/requests")
                        .header("X-Idempotency-Key", idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Idempotency Test"))
                .andReturn();

        CreateRequestDto diffDto = new CreateRequestDto();
        diffDto.setName("Different Name");
        diffDto.setContent("Different Content");

        MvcResult secondResult = mockMvc.perform(post("/api/requests")
                        .header("X-Idempotency-Key", idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diffDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Idempotency Test"))
                .andExpect(jsonPath("$.content").value("Initial Content"))
                .andReturn();

        assertEquals(firstResult.getResponse().getContentAsString(),
                secondResult.getResponse().getContentAsString());
    }

    @Test
    void shouldReturnConflictWhenProcessingSimulatedTimeout() throws Exception {
        UUID idempotencyKey = UUID.randomUUID();
        IdempotencyRecordEntity record =
                IdempotencyRecordEntity.builder()
                        .idempotencyKey(idempotencyKey)
                        .createdAt(Instant.now())
                        .build();
        idempotencyRepository.saveAndFlush(record);

        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setName("Timeout Test");
        createDto.setContent("Content");

        mockMvc.perform(post("/api/requests")
                        .header("X-Idempotency-Key", idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Request is currently being processed"));
    }

    @Test
    void shouldReturnBadRequestForUnknownState() throws Exception {
        mockMvc.perform(get("/api/requests?state=INVALID_STATE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid parameter or body format"));
    }

    @Test
    void shouldReturnBadRequestForInvalidIdempotencyKey() throws Exception {
        CreateRequestDto createDto = new CreateRequestDto();
        createDto.setName("Invalid Key Test");
        createDto.setContent("Content");

        mockMvc.perform(post("/api/requests")
                        .header("X-Idempotency-Key", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("X-Idempotency-Key must be a valid UUID"));
    }

    @Test
    void shouldReturnBadRequestForNegativePageOrSize() throws Exception {
        mockMvc.perform(get("/api/requests?page=-1&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/requests?page=0&size=-5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
