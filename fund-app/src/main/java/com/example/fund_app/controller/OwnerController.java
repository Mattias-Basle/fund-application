package com.example.fund_app.controller;

import com.example.fund_app.mapper.OwnerMapper;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dto.OwnerDetailsViewDto;
import com.example.fund_app.model.dto.OwnerViewDto;
import com.example.fund_app.service.OwnerService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/owners")
public class OwnerController {

    private final OwnerService ownerService;
    private final OwnerMapper ownerMapper;

    public OwnerController(OwnerService ownerService, OwnerMapper ownerMapper) {
        this.ownerService = ownerService;
        this.ownerMapper = ownerMapper;
    }

    @PostMapping
    public ResponseEntity<Void> createOwner(
            @Pattern(regexp = "^\\D{3,15}")
            @RequestParam String name) {
        ownerService.createOwner(name);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<Page<OwnerViewDto>> getAllOwners(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer pageNumber,
            @Min(10L) @Max(100L) @RequestParam(name = "size", required = false, defaultValue = "10") Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<Owner> ownerList = ownerService.getAllOwners(pageable);
        return ResponseEntity.ok(ownerMapper.toDto(ownerList));
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<OwnerViewDto> getOwnerById(@PathVariable Long ownerId) {
        Owner response = ownerService.getById(ownerId);
        return ResponseEntity.ok(ownerMapper.toDto(response));
    }

    @GetMapping("/{ownerId}/details")
    public ResponseEntity<OwnerDetailsViewDto> getOwnerDetailsById(@PathVariable Long ownerId) {
        Owner response = ownerService.getById(ownerId);
        return ResponseEntity.ok(ownerMapper.toDetailsDto(response));
    }

    @PatchMapping("/{ownerId}")
    public ResponseEntity<Void> addAccount(
            @PathVariable Long ownerId,
            @RequestParam Currency currency) {
        ownerService.addAccountToOwner(ownerId, currency);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{ownerId}")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long ownerId) {
        ownerService.deleteOwner(ownerId);
        return ResponseEntity.noContent().build();
    }
}
