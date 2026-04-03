package com.petreca.petrecadelivery.courier.management.api.doc;

import com.petreca.petrecadelivery.courier.management.api.model.CourierInput;
import com.petreca.petrecadelivery.courier.management.api.model.CourierPayoutCalculationInput;
import com.petreca.petrecadelivery.courier.management.api.model.CourierPayoutResultModel;
import com.petreca.petrecadelivery.courier.management.domain.model.Courier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * API Contract for Courier Management.
 * Centralizes routing, Swagger/OpenAPI documentation, validation, and REST semantics.
 */
@Tag(name = "Couriers API", description = "Operations related to courier registration and payout management")
@RequestMapping("/api/v1/couriers")
public interface CourierControllerDoc {

    @Operation(summary = "Register a new Courier", description = "Creates a new courier profile. Returns the resource Location URL")
    @ApiResponse(responseCode = "201", description = "Courier registered successfully",
            headers = @Header(name = "Location", description = "URI of the newly created courier"))
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    @PostMapping
    ResponseEntity<Void> create(@Valid @RequestBody CourierInput courierInput);

    @Operation(summary = "Update a Courier data", description = "Updates an existing courier's information and returns the updated state.")
    @ApiResponse(responseCode = "200", description = "Courier updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Courier not found")
    @PutMapping("/{courierId}")
    ResponseEntity<Courier> update(@PathVariable("courierId")UUID courierId,
                                   @Valid @RequestBody CourierInput input);

    @Operation(summary = "List all couriers", description = "Returns a paginated list of all couriers in the system.")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @GetMapping
    ResponseEntity<PagedModel<Courier>> findAll(@ParameterObject @PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable);

    @Operation(summary = "Find courier by ID", description = "Retrieves a specific courier's details by their UUID.")
    @ApiResponse(responseCode = "200", description = "Courier found")
    @ApiResponse(responseCode = "404", description = "Courier not found")
    @GetMapping("/{courierId}")
    ResponseEntity<Courier> findById(@PathVariable("courierId") UUID courierId);

    @Operation(summary = "Calculate courier payout", description = "Calculates the payment fee based on distance traveled.")
    @ApiResponse(responseCode = "200", description = "Payout calculated successfully")
    @PostMapping("/payout-calculation")
    ResponseEntity<CourierPayoutResultModel> calculate(@Valid @RequestBody CourierPayoutCalculationInput input);
}
