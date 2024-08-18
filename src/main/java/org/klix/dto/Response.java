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
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private SourceSystem sourceSystem;
    private String status;
    private Offer offer;
    private List<ErrorDetail> errors;

}
