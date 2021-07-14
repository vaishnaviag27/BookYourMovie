package com.ebooking.BookUrMovie.managements.services;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.AddOn;
import com.ebooking.BookUrMovie.managements.repositories.AddOnRepository;
import com.ebooking.BookUrMovie.managements.utils.NonNullObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

;
import java.util.List;

@Service
@Slf4j
public class AddonService {

    @Autowired
    AddOnRepository addOnRepository;

    public List<AddOn> listAllAddon() throws NotFoundException {
        List<AddOn> addons = addOnRepository.findAll();
        if(addons.isEmpty()){
            throw new NotFoundException("Addons Not found!");
        }
        return addons;
    }

    public AddOn getAddonById(String addonName) throws NotFoundException {
        log.debug("Fetching Addon with name: {}", addonName);
        AddOn addon = addOnRepository.findByAddonName(addonName);
        if(addon == null){
            log.error("Addon not found with Name: {}", addonName);
            throw new NotFoundException("Addon not found with Name: " + addonName);
        }
        return addon;
    }

    public String addAddon(AddOn newAddon) throws BadDetailsException, IllegalArgumentException {
        log.debug("Adding addon with name: {}", newAddon.getAddonName());
        if (addOnRepository.existsByAddonName(newAddon.getAddonName())) {
            log.error("Addon already exists: {}", newAddon.getAddonName());
            throw new IllegalArgumentException("Addon already exists " + newAddon.getAddonName());
        }
        String validateAddonsResponse = validateAddons(newAddon, true);
        if (!validateAddonsResponse.equals("valid")) {
            log.error("Addon Details are invalid: {}", validateAddonsResponse);
            throw new BadDetailsException("Addon Details are invalid: " + validateAddonsResponse);
        }
        addOnRepository.save(newAddon);
        log.debug("Added addon successfully: {}", newAddon.getAddonName());
        return "Add-On Added Successfully!";
    }

    public String updateAddon(AddOn updatedAddon) throws NotFoundException, BadDetailsException {
        AddOn foundAddon = addOnRepository.findByAddonName(updatedAddon.getAddonName());
        if (foundAddon == null) {
            log.error("Addon Does not exists: {}", updatedAddon.getAddonName());
            throw new NotFoundException("Addon does not exists " + updatedAddon.getAddonName());
        }
        String validateAddonsResponse = validateAddons(updatedAddon, false);
        if (!validateAddonsResponse.equals("valid")) {
            log.error("Addon Details are invalid: {}", validateAddonsResponse);
            throw new BadDetailsException("Addon Details are invalid: " + validateAddonsResponse);
        }
        NonNullObjectMapper.copyNonNullProperties(updatedAddon, foundAddon);
        addOnRepository.save(foundAddon);
        log.debug("Addon updated successfully: {}", updatedAddon.getAddonName());
        return "Addon Updated!";
    }

    public String disableOrEnableAddon(String addonName) throws NotFoundException {
        log.debug("Enabling/Disabling Addon availability: {}", addonName);
        AddOn foundAddon = addOnRepository.findByAddonName(addonName);
        if (foundAddon == null) {
            log.error("Addon Does not exists: {}", addonName);
            throw new NotFoundException("Addon does not exists " + addonName);
        }

        if (foundAddon.getAddonIsAvailable()) {
            log.debug("Setting addon availability to false", addonName);
            foundAddon.setAddonIsAvailable(false);
        } else {
            log.debug("Setting addon availability to true", addonName);
            foundAddon.setAddonIsAvailable(true);
        }
        addOnRepository.save(foundAddon);
        return "Addon availability set to: " + foundAddon.getAddonIsAvailable();
    }

    @Transactional
    public String deleteAddOn(String addonName) throws NotFoundException {
        log.debug("Deleting add-on: {}", addonName);
        if (!addOnRepository.existsByAddonName(addonName)) {
            log.error("Addon Does not exists: {}", addonName);
            throw new NotFoundException("Addon does not exists " + addonName);
        }
        addOnRepository.deleteByAddonName(addonName);
        log.debug("Addon deleted successfully: {}", addonName);
        return "Addon Deleted Successfully!";
    }

    private String validateAddons(AddOn addOn, boolean isNewAddon) {
        if (isNewAddon) {
            if (addOn.getAddonName() == null) {
                return "Please enter AddOn Name!";
            }
            if (addOn.getAddonPrice() == 0.00f) {
                return "Please Enter AddOn Price!";
            }
        }
        if (addOn.getAddonName().length() < 3) {
            return "AddOn Name Must have at least 3 characters";
        }
        if (addOn.getAddonPrice() < 0.00f) {
            return "Addon Price Cannot be less than or equal to 0.00";
        }
        return "valid";
    }
}
