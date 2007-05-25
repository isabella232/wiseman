package com.sun.ws.management.client;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;

import com.sun.ws.management.client.exceptions.FaultException;
import com.sun.ws.management.client.impl.JAXBResourceImpl.ResourceEnumerator;
import com.sun.ws.management.xml.XmlBinding;

public interface JAXBResource {

	/**
	 * Fetches the model contents from the WS-Management Resource.
	 * 
	 * @return the model.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract Object getObject() throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException;

	/**
	 * Updates the WS-Management Resource with the new model.
	 * 
	 * @param newState -
	 *            the new model. newState is a JAXB enabled object.
	 * @return new resource model state, if different from that specified in
	 *         newState.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract Object putObject(Object newState) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	/**
	 * @return Returns the WS-Management resource epr.
	 */
	public abstract EndpointReferenceType getEpr();

	/**
	 * @param epr
	 *            The WS-Management resource epr.
	 */
	public abstract void setEpr(EndpointReferenceType epr);

	/**
	 * @return Returns the binding for transforming the between JAVA objects and
	 *         XML representation.
	 */
	public abstract XmlBinding getBinding();

	/**
	 * @param binding
	 *            The binding for transforming the between JAVA objects and XML
	 *            representation.
	 */
	public abstract void setBinding(XmlBinding binding);

	/**
	 * Creates the binding for transforming the between JAVA objects and XML
	 * representation.
	 * 
	 * @param packageNames -
	 *            List of JAXB generated packages to use
	 * @throws JAXBException
	 */
	public abstract void setBinding(final String... packageNames)
			throws JAXBException;

	/**
	 * Establishs a enumeration session with the WS-Management resource.
	 * 
	 * @param filters -
	 *            enumerate filters specified in the nominated dialect
	 * @param dialect -
	 *            filter dialect
	 * @param mode -
	 *            enumeration mode.
	 *            Either null (Enumerate Objects), ENUMERATE_EPR or EnumerateObjectAndEPR
	 * @return the enumeration iterator.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceEnumerator enumerate(String[] filters,
			String dialect, EnumerationModeType mode) throws SOAPException,
			JAXBException, IOException, FaultException,
			DatatypeConfigurationException;

	/**
	 * Establishs a enumeration session with the WS-Management resource.
	 * 
	 * @param filters -
	 *            enumerate filters specified in the XPATH-1 dialect
	 * @param mode -
	 *            enumeration mode.
	 *            Either null (Enumerate Objects), ENUMERATE_EPR or EnumerateObjectAndEPR
	 * @return the enumeration iterator.
	 * @throws SOAPException
	 * @throws JAXBException
	 * @throws IOException
	 * @throws FaultException
	 * @throws DatatypeConfigurationException
	 */
	public abstract ResourceEnumerator enumerate(String[] filters,
			EnumerationModeType mode) throws SOAPException, JAXBException,
			IOException, FaultException, DatatypeConfigurationException;

}