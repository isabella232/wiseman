package com.sun.ws.management.framework.transfer;

import com.sun.ws.management.Management;
import com.sun.ws.management.soap.SenderFault;
import com.sun.ws.management.transfer.InvalidRepresentationFault;

public interface Transferable {
    public void create(Management request,Management response) throws InvalidRepresentationFault;
    public void delete(Management request,Management response) throws SenderFault;
    public void get(Management request, Management response) throws SenderFault;
    public void put( Management  request, Management response) throws InvalidRepresentationFault, SenderFault;
 
}

