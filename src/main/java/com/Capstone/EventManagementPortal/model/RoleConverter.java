package com.Capstone.EventManagementPortal.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        // Directly stores values as "ADMIN", "ORGANIZER", etc. without "ROLE_" prefix
        return role.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        // Directly converts the database value to Role enum
        return Role.valueOf(dbData);
    }
}
