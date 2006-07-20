package com.sun.ws.management.framework.enumeration;

import com.sun.ws.management.enumeration.Enumeration;

/**
 *
 */
public interface Enumeratable {
    void release(Enumeration enuRequest, Enumeration enuResponse);

    void pull(Enumeration enuRequest, Enumeration enuResponse);

    void enumerate(Enumeration enuRequest, Enumeration enuResponse);

    void getStatus(Enumeration enuRequest, Enumeration enuResponse);

    void renew(Enumeration enuRequest, Enumeration enuResponse);
}
