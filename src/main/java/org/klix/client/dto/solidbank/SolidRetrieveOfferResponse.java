package org.klix.client.dto.solidbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SolidRetrieveOfferResponse {

    private String status;
    private SolidOfferResponse offer;

}
