package org.klix.client.dto.solidbank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolidSubmitOfferResponse {

    private String id;
    private ApplicationStatus status;

}
