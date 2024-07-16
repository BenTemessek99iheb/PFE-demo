package org.acme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.model.charts.Graphique;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphiqueRequest {
    private Graphique graphique;
    private List<Long> deviceIds;

}
