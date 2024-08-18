package org.klix.client.dto.solidbank;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SolidRetrieveOfferResponse {

    private String status;
    private SolidOfferResponse offer;

}
