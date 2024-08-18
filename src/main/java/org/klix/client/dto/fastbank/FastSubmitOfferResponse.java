package org.klix.client.dto.fastbank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FastSubmitOfferResponse {

    private String id;
    private ApplicationStatus status;

}
