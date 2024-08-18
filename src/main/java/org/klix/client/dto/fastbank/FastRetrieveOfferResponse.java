package org.klix.client.dto.fastbank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FastRetrieveOfferResponse {

    private String status;
    private FastOfferResponse offer;

}
