package com.Capstone.EventManagementPortal.model;

import com.Capstone.EventManagementPortal.model.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        return "ROLE_" + role.name(); // Store as ROLE_ADMIN, ROLE_ORGANIZER, etc.
    }

    @Override
    public Role convertToEntityAttribute(String dbRole) {
        return Role.valueOf(dbRole.replace("ROLE_", "")); // Convert back to Java enum
    }
}
