package org.klix.error;

import java.io.Serializable;

public interface ErrorType extends Serializable {

    String getCode();

    String getMessage();

}
