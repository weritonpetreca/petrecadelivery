package com.petreca.petrecadelivery.delivery.tracking.api.doc;

import com.petreca.petrecadelivery.delivery.tracking.api.model.CourierIdInput;
import com.petreca.petrecadelivery.delivery.tracking.api.model.DeliveryInput;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.Delivery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Deliveries", description = "Operations related to the delivery tracking lifecycles")
@RequestMapping("/api/v1/deliveries")
public interface DeliveryControllerDoc {

    @Operation(summary = "Draft a new delivery", description = "Creates a new delivery draft. Returns the Location URI of the created resource.")
    @ApiResponse(responseCode = "201", description = "Delivery drafted successfully",
            headers = @Header(name = "Location", description = "URI of the newly drafted delivery"))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping
    ResponseEntity<Void> draft(@RequestBody @Valid DeliveryInput input);

    @Operation(summary = "Edit delivery", description = "Edit the details of an existing delivery draft.")
    @ApiResponse(responseCode = "200", description = "Delivery edited successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Delivery nor found")
    @PutMapping("/{deliveryId}")
    ResponseEntity<Delivery> edit(@PathVariable("deliveryId") UUID deliveryId, @RequestBody @Valid DeliveryInput input);

    @Operation(summary = "List deliveries", description = "Retrieves a paginated list of all deliveries.")
    @ApiResponse(responseCode = "200", description = "Deliveries retrieved successfully")
    @GetMapping
    ResponseEntity<PagedModel<Delivery>> findAll(@ParameterObject @PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable);

    @Operation(summary = "Find delivery by ID", description = "Retrieves the details of a specific delivery.")
    @ApiResponse(responseCode = "200", description = "Delivery found")
    @ApiResponse(responseCode = "404", description = "Delivery not found")
    @GetMapping("/{deliveryId}")
    ResponseEntity<Delivery> findById(@PathVariable("deliveryId") UUID deliveryId);

    @Operation(summary = "Place delivery", description = "Transitions the delivery state to PLACED.")
    @ApiResponse(responseCode = "204", description = "Delivery placed successfully")
    @ApiResponse(responseCode = "404", description = "Delivery not found")
    @PostMapping("/{deliveryId}/placement")
    ResponseEntity<Void> place(@PathVariable("deliveryId") UUID deliveryId);

    @Operation(summary = "Pick up delivery", description = "Assigns a courier and transitions the delivery state to PICKED_UP.")
    @ApiResponse(responseCode = "204", description = "Delivery picked up successfully")
    @ApiResponse(responseCode = "400", description = "Invalid courier input")
    @ApiResponse(responseCode = "404", description = "Delivery not found")
    @PostMapping("/{deliveryId}/pickups")
    ResponseEntity<Void> pickUp(@PathVariable("deliveryId") UUID deliveryId, @RequestBody @Valid CourierIdInput input);

    @Operation(summary = "Complete delivery", description = "Transitions the delivery state to COMPLETED.")
    @ApiResponse(responseCode = "204", description = "Delivery completed successfully")
    @ApiResponse(responseCode = "404", description = "Delivery not found")
    @PostMapping("/{deliveryId}/completion")
    ResponseEntity<Void> complete(@PathVariable("deliveryId") UUID deliveryId);
}
