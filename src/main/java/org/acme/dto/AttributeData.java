package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeData {
    private Timestamp date;
    private Map<String, Object> attributes = new HashMap<>();

    // getters and setters

    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

}
