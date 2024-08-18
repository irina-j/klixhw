package org.klix.client.dto.fastbank;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Application {

    private String id;
    private ApplicationStatus status;
    private FastRetrieveOfferResponse fastRetrieveOfferResponse;

}
