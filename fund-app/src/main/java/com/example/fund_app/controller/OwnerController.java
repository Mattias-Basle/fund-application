package com.example.fund_app.controller;

import com.example.fund_app.mapper.OwnerMapper;
import com.example.fund_app.model.Currency;
import com.example.fund_app.model.Owner;
import com.example.fund_app.model.dto.OwnerViewDto;
import com.example.fund_app.service.OwnerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Void> createOwner(@RequestParam String name) {
        ownerService.createOwner(name);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<OwnerViewDto>> getAllOwners() {
        List<Owner> ownerList = ownerService.getAllOwners();
        return ResponseEntity.ok(ownerMapper.toDto(ownerList));
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<OwnerViewDto> getOwnerById(@PathVariable Long ownerId) {
        Owner response = ownerService.getById(ownerId);
        return ResponseEntity.ok(ownerMapper.toDto(response));
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
