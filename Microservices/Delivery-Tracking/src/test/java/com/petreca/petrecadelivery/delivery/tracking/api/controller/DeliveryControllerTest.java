package com.petreca.petrecadelivery.delivery.tracking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petreca.petrecadelivery.delivery.tracking.api.model.ContactPointInput;
import com.petreca.petrecadelivery.delivery.tracking.api.model.DeliveryInput;
import com.petreca.petrecadelivery.delivery.tracking.api.model.ItemInput;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.Delivery;
import com.petreca.petrecadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import com.petreca.petrecadelivery.delivery.tracking.domain.service.DeliveryCheckpointService;
import com.petreca.petrecadelivery.delivery.tracking.domain.service.DeliveryPreparationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Delivery Controller API Tests")
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeliveryPreparationService preparationService;

    @MockitoBean
    private DeliveryCheckpointService checkpointService;

    @MockitoBean
    private DeliveryRepository deliveryRepository;

    private DeliveryInput createValidDeliveryInput() {
        ContactPointInput sender = new ContactPointInput(
                "12345-000", "Street", "10", "Apt", "Sender", "119999");
        ContactPointInput recipient = new ContactPointInput(
                "54321-000", "Avenue", "20", "", "Recipient", "118888");
        List<ItemInput> items = List.of(new ItemInput("Sword", 1));

        return new DeliveryInput(sender, recipient, items);
    }

    @Nested
    @DisplayName("POST /api/v1/deliveries")
    class DraftDelivery {

        @Test
        @DisplayName("should return 201 Created when valid payload is provided")
        void shouldDraftDelivery() throws Exception {
            DeliveryInput input = createValidDeliveryInput();
            Delivery mockDelivery = Delivery.draft();

            given(preparationService.draft(any(DeliveryInput.class))).willReturn(mockDelivery);

            mockMvc.perform(post("/api/v1/deliveries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated());

            then(preparationService).should().draft(any(DeliveryInput.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/deliveries/{id}")
    class GetDelivery {

        @Test
        @DisplayName("should return 200 OK when delivery exists")
        void shouldGetDeliveryById() throws Exception {
            UUID deliveryId = UUID.randomUUID();
            Delivery mockDelivery = Delivery.draft();

            given(deliveryRepository.findById(deliveryId)).willReturn(Optional.of(mockDelivery));

            mockMvc.perform(get("/api/v1/deliveries/{id}", deliveryId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 404 Not Found when delivery does not exist")
        void shouldReturn404WhenDeliveryNotFound() throws Exception {
            UUID deliveryId = UUID.randomUUID();
            given(deliveryRepository.findById(deliveryId)).willReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/deliveries/{id}", deliveryId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/deliveries/{id}/placement")
    class PlaceDelivery {

        @Test
        @DisplayName("should return 204 No Content when successful")
        void shouldPlaceDelivery() throws Exception {
            UUID deliveryId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/deliveries/{id}/placement", deliveryId))
                    .andExpect(status().isNoContent());

            then(checkpointService).should().place(deliveryId);
        }

    }

    @Nested
    @DisplayName("POST /api/v1/deliveries/{id}/completion")
    class CompleteDelivery {

        @Test
        @DisplayName("should return 204 No Content when successful")
        void shouldCompleteDelivery() throws Exception {
            UUID deliveryId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/deliveries/{id}/completion", deliveryId))
                    .andExpect(status().isNoContent());

            then(checkpointService).should().complete(deliveryId);
        }
    }
}
