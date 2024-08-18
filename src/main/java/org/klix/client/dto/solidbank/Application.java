package org.klix.client.dto.solidbank;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Application {

    private String id;
    private ApplicationStatus status;
    private SolidRetrieveOfferResponse solidRetrieveOfferResponse;

}
