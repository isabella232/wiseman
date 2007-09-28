
package com.hp.itil._2007._05.servicetype.servicetypemodelservice.servicetypemodelresource;

import com.hp.itil._2007._05.servicetype.ObjectFactory;
import com.hp.itil._2007._05.servicetype.ServiceTypeType;
import com.sun.ws.management.InternalErrorFault;
import com.sun.ws.management.InvalidSelectorsFault;
import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.Management;
import com.sun.ws.management.framework.Utilities;
import com.sun.ws.management.framework.handlers.ResourceHandler;
import com.sun.ws.management.server.EnumerationSupport;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.transfer.InvalidRepresentationFault;
import com.sun.ws.management.transfer.TransferExtensions;
import com.sun.ws.management.transport.ContentType;

import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;



import org.dmtf.schemas.wbem.wsman._1.wsman.SelectorType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;


/**
 * ServicetypemodelresourceHandler delegate is responsible for processing enumeration actions.
 *
 * @GENERATED
 */
public class ServicetypemodelresourceHandler extends ResourceHandler {

    public static final String resourceURI = "http://www.hp.com/itil/2007/05/ServiceType/ServiceTypeModelService/ServiceTypeModelResource";
    HashMap<String, ServiceTypeType> data = new HashMap<String, ServiceTypeType>();
    
	private static final ObjectFactory svcTypeFactory = new ObjectFactory();

       
    // Log for logging messages
    @SuppressWarnings("unused")
    private final Logger log;
    
	private static final QName QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "ServiceTypeModel");
    

    public ServicetypemodelresourceHandler() {
        super();
        
        // Initialize our member variables
        log = Logger.getLogger(ServicetypemodelresourceHandler.class.getName());
        try {
            // Register the IteratorFactory with EnumerationSupport
            EnumerationSupport.registerIteratorFactory("http://www.hp.com/itil/2007/05/ServiceType/ServiceTypeModelService/ServiceTypeModelResource",
                                                       new ServicetypemodelresourceIteratorFactory("http://www.hp.com/itil/2007/05/ServiceType/ServiceTypeModelService/ServiceTypeModelResource"));
        } catch (Exception e) {
            throw new InternalErrorFault(e);
        }
    }
    public void Get(HandlerContext context, Management request, Management response) {
    	
    	// get the selector from the request
    	
		try {
			String version = getVersionSelector(request);
			// Look up service type in hash map
			ServiceTypeType svc = data.get(version);
			if (svc != null) {
				TransferExtensions xferRequest = new TransferExtensions(request);
				TransferExtensions xferResponse = new TransferExtensions(
						response);

				xferResponse.setFragmentGetResponse(
						xferRequest.getFragmentHeader(), svcTypeFactory.createServiceTypeModel(svc));
	 			
				xferResponse.setContentType(ContentType.ATTACHMENT_CONTENT_TYPE);
				response.setContentType(ContentType.ATTACHMENT_CONTENT_TYPE);
				AttachmentPart attach = xferResponse.getMessage().createAttachmentPart();
//				attach.setContentType("text/html");
				attach.setContentId("myData");
				attach.setContent("This is my plain text attachment", "text/plain");
				xferResponse.getMessage().addAttachmentPart(attach);


			}
		} catch (Exception e) {
			e.printStackTrace();
		}    	
        
    }

    public void RenewSubscriptionOp(HandlerContext context, Eventing request, Eventing response) {
        // TODO: For subscribe:
        //       Call EventSupport.subscribe() & save the UUID returned (use ContextListener to detect subscribe/unsubscribes)
        //       Start sending events to EventSupport.sendEvent(uuid, event)
        
        // TODO: For unsubscribe:
        //       Call EventSupport.unsubscribe()
        //       Stop sending events for this UUID
        super.renewSubscription(context, request, response);
    } 

    public void ReleaseOp(HandlerContext context, Enumeration request, Enumeration response) {
        super.release(context, request, response);
    }    
    public void Delete(HandlerContext context, Management request, Management response) {
    	// get the selector from the request
    	
		try {
			String version = getVersionSelector(request);
			// Look up service type in hash map
			ServiceTypeType svc = data.get(version);
			if (svc != null) {
				data.remove(version);
				TransferExtensions xferResponse = new TransferExtensions(response);

				xferResponse.setDeleteResponse();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}    	
    }

    public void SubscribeOp(HandlerContext context, Eventing request, Eventing response) {
        // TODO: For subscribe:
        //       Call EventSupport.subscribe() & save the UUID returned (use ContextListener to detect subscribe/unsubscribes)
        //       Start sending events to EventSupport.sendEvent(uuid, event)
        
        // TODO: For unsubscribe:
        //       Call EventSupport.unsubscribe()
        //       Stop sending events for this UUID
        super.subscribe(context, request, response);
    } 

    public void EnumerateOp(HandlerContext context, Enumeration request, Enumeration response) {
        super.enumerate(context, request, response);
    }    
    public void Create(HandlerContext context, Management request, Management response) {
		try {
			// TODO: Implement Create here and remove the following call to super
			// Get the resource passed in the body
			TransferExtensions xferRequest = new TransferExtensions(request);
			TransferExtensions xferResponse = new TransferExtensions(response);
			Object element = xferRequest.getResource(QNAME);
			ServiceTypeType svcType;
			if (element != null) {
				JAXBElement<ServiceTypeType> svcElement = getResource(request);
				
				ServiceTypeType newSvc = svcElement.getValue();
				
				
				data.put(newSvc.getVersion(), newSvc);
				
				// Define a selector (in this case version)
				HashMap<String, String> selectors = new HashMap<String, String>();
				selectors.put("version", newSvc.getVersion());
				
				EndpointReferenceType epr = xferResponse.createEndpointReference(
						request.getTo(), request.getResourceURI(), selectors);
				xferResponse.setCreateResponse(epr);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}        
    }

    public void UnsubscribeOp(HandlerContext context, Eventing request, Eventing response) {
        // TODO: For subscribe:
        //       Call EventSupport.subscribe() & save the UUID returned (use ContextListener to detect subscribe/unsubscribes)
        //       Start sending events to EventSupport.sendEvent(uuid, event)
        
        // TODO: For unsubscribe:
        //       Call EventSupport.unsubscribe()
        //       Stop sending events for this UUID
        super.unsubscribe(context, request, response);
    } 
    public void Put(HandlerContext context, Management request, Management response) {
		String version = getVersionSelector(request);
		// Look up service type in hash map
		ServiceTypeType svc = data.get(version);
		
		if (svc == null) {
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}

		// Get the resource passed in the body
		Object obj = getResource(request);
		if ((obj instanceof JAXBElement) == false) {
			throw new InternalErrorFault("Wrong resource type \n");
		}

		JAXBElement<ServiceTypeType> tlElement = (JAXBElement<ServiceTypeType>) obj;
		ServiceTypeType svcType = tlElement.getValue();

		// Transfer values
		svc.setRollbackPolicy(svcType.getRollbackPolicy());
		svc.setServiceDefinition(svcType.getServiceDefinition());
		svc.setServiceTypeACL(svcType.getServiceTypeACL());
		svc.setServiceTypeMetadata(svcType.getServiceTypeMetadata());
		svc.setType(svcType.getType());
		svc.setVersion(svcType.getVersion());

		try {
			TransferExtensions xferResponse = new TransferExtensions(response);

			xferResponse.setPutResponse(obj);
		} catch (Exception e) {
			throw new InternalErrorFault(e);
		}
    }

    public void PullOp(HandlerContext context, Enumeration request, Enumeration response) {
        super.pull(context, request, response);
    }    

    public void GetStatusOp(HandlerContext context, Enumeration request, Enumeration response) {
        super.getStatus(context, request, response);
    }    

    public void RenewOp(HandlerContext context, Enumeration request, Enumeration response) {
        super.renew(context, request, response);
    }    

    public void GetSubscriptionStatusOp(HandlerContext context, Eventing request, Eventing response) {
        // TODO: For subscribe:
        //       Call EventSupport.subscribe() & save the UUID returned (use ContextListener to detect subscribe/unsubscribes)
        //       Start sending events to EventSupport.sendEvent(uuid, event)
        
        // TODO: For unsubscribe:
        //       Call EventSupport.unsubscribe()
        //       Stop sending events for this UUID
        super.getSubscriptionStatus(context, request, response);
    }
    
	private static JAXBElement<ServiceTypeType> getResource(Management request) {
		JAXBElement<ServiceTypeType> tlElement;

		try {
			// Get JAXB Representation of Soap Body property document
			TransferExtensions transfer = new TransferExtensions(request);

			Object element = transfer.getResource(QNAME);
			
			if (element == null) {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.MISSING_VALUES);
			}

			if (element instanceof JAXBElement) {
				if (((JAXBElement) element).getDeclaredType().equals(
						ServiceTypeType.class)) {
					tlElement = (JAXBElement<ServiceTypeType>) element;
				} else {
					// XmlFragment only supported on Get
					throw new UnsupportedFeatureFault(
							UnsupportedFeatureFault.Detail.FRAGMENT_LEVEL_ACCESS);
				}
			} else {
				throw new InvalidRepresentationFault(
						InvalidRepresentationFault.Detail.INVALID_VALUES);
			}
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		}
		return tlElement;
	}

	private static String getVersionSelector(Management request)
	throws InternalErrorFault {
		Set<SelectorType> selectors;
		try {
			selectors = request.getSelectors();
		} catch (JAXBException e) {
			throw new InternalErrorFault(e);
		} catch (SOAPException e) {
			throw new InternalErrorFault(e);
		}
		if (Utilities.getSelectorByName("version", selectors) == null) {
			throw new InvalidSelectorsFault(
					InvalidSelectorsFault.Detail.INSUFFICIENT_SELECTORS);
		}
		return (String) Utilities.getSelectorByName("version", selectors)
				.getContent().get(0);
	}

}