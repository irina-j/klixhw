package org.klix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.klix.error.ErrorDetail;
import org.klix.service.SourceSystem;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationResponse {

    public ApplicationResponse(String id, String status) {
        this.id = id;
        this.status = status;
    }

    private String id;
    private SourceSystem sourceSystem;
    private String status;
    private List<ErrorDetail> errors;

}
