/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: EnumerationExtensions.java,v 1.11 2007-01-14 17:52:32 denis_rachal Exp $
 */

package com.sun.ws.management.enumeration;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.dmtf.schemas.wbem.wsman._1.wsman.AnyListType;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableEmpty;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributableNonNegativeInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.AttributablePositiveInteger;
import org.dmtf.schemas.wbem.wsman._1.wsman.DialectableMixedDataType;
import org.dmtf.schemas.wbem.wsman._1.wsman.EnumerationModeType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlsoap.schemas.ws._2004._08.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.Enumerate;
import org.xmlsoap.schemas.ws._2004._09.enumeration.EnumerateResponse;
import org.xmlsoap.schemas.ws._2004._09.enumeration.ItemListType;
import org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.xml.XPath;
import com.sun.ws.management.xml.XmlBinding;

public class EnumerationExtensions extends Enumeration {
    
    public static final QName REQUEST_TOTAL_ITEMS_COUNT_ESTIMATE =
            new QName(Management.NS_URI, "RequestTotalItemsCountEstimate", Management.NS_PREFIX);
    
    public static final QName TOTAL_ITEMS_COUNT_ESTIMATE =
            new QName(Management.NS_URI, "TotalItemsCountEstimate", Management.NS_PREFIX);
    
    public static final QName OPTIMIZE_ENUMERATION =
            new QName(Management.NS_URI, "OptimizeEnumeration", Management.NS_PREFIX);
    
    public static final QName MAX_ELEMENTS =
            new QName(Management.NS_URI, "MaxElements", Management.NS_PREFIX);
    
    public static final QName ENUMERATION_MODE =
            new QName(Management.NS_URI, "EnumerationMode", Management.NS_PREFIX);
    
    public static final QName ITEMS =
            new QName(Management.NS_URI, "Items", Management.NS_PREFIX);
    
    public static final QName ITEM =
            new QName(Management.NS_URI, "Item", Management.NS_PREFIX);
    
    public static final QName END_OF_SEQUENCE =
            new QName(Management.NS_URI, "EndOfSequence", Management.NS_PREFIX);
    
    public static final QName FILTER =
        new QName(Management.NS_URI, "Filter", Management.NS_PREFIX);
    
    final static String WSMAN_ITEM = ITEM.getPrefix()+":"+ITEM.getLocalPart();

    public enum Mode {
        EnumerateEPR("EnumerateEPR"),
        EnumerateObjectAndEPR("EnumerateObjectAndEPR");
        
        public static Mode fromBinding(final JAXBElement<EnumerationModeType> t) {
            return valueOf(t.getValue().value());
        }
        
        private String mode;
        Mode(final String m) { mode = m; }
        public JAXBElement<EnumerationModeType> toBinding() {
            return Management.FACTORY.createEnumerationMode(EnumerationModeType.fromValue(mode));
        }
    }
    
    public EnumerationExtensions() throws SOAPException {
        super();
    }
    
    public EnumerationExtensions(final Addressing addr) throws SOAPException {
        super(addr);
    }
    
    public EnumerationExtensions(final InputStream is) throws SOAPException, IOException {
        super(is);
    }
    
    public void setEnumerate(final EndpointReferenceType endTo,
    		                 final boolean requestTotalItemsCountEstimate,
                             final boolean optimize,
                             final int maxElements,
                             final String expires,
                             final DialectableMixedDataType filter,
                             final Mode mode,
                             final Object... anys) throws JAXBException, SOAPException {
               
        ArrayList<Object> list = new ArrayList<Object>();

        if (filter != null) {
        	list.add(Management.FACTORY.createFilter(filter));
        }
        if (mode != null) {
        	list.add(mode.toBinding());
        }
        if (optimize == true) {
        	list.add(Management.FACTORY.createOptimizeEnumeration(new AttributableEmpty()));
        	
            if (maxElements > 0) {
                final AttributablePositiveInteger posInt = new AttributablePositiveInteger();
                posInt.setValue(new BigInteger(Integer.toString(maxElements)));
                list.add(Management.FACTORY.createMaxElements(posInt));
            }
        }

        for (int i = 0; i < anys.length; i++) {
        	list.add(anys[i]);
        }
        super.setEnumerate(endTo, expires, null, list.toArray());
        if (requestTotalItemsCountEstimate)
           setRequestTotalItemsCountEstimate();
    }
    
    // context must not be null, the others can be null
    // context must be either java.lang.String or org.w3c.dom.Element
    public void setPull(final Object context, final int maxChars,
            final int maxElements, final Duration maxDuration,
            final boolean requestTotalItemsCountEstimate)
            throws JAXBException, SOAPException, DatatypeConfigurationException {
    	
    	super.setPull(context, maxChars, maxElements, maxDuration);
        if (requestTotalItemsCountEstimate)
            setRequestTotalItemsCountEstimate();
    }
    
    public void setEnumerateResponse(final Object context, final String expires,
            final List<EnumerationItem> items, final EnumerationModeType mode, final boolean haveMore)
            throws JAXBException, SOAPException {
        
        final AnyListType anyListType = Management.FACTORY.createAnyListType();
        final List<Object> any = anyListType.getAny();
        final DocumentBuilder builder = getDocumentBuilder();
        final XmlBinding binding = getXmlBinding();
        if (items != null) {
			for (final EnumerationItem ee : items) {
				addEnumerationItem(any, ee, mode, builder, binding);
			}

			JAXBElement anyList = Management.FACTORY.createItems(anyListType);
			if (!haveMore) {
				JAXBElement<AttributableEmpty> eos = Management.FACTORY
						.createEndOfSequence(new AttributableEmpty());
				super.setEnumerateResponse(context, expires, anyList, eos);
			} else {
				super.setEnumerateResponse(context, expires, anyList);
			}
		} else {

			super.setEnumerateResponse(context, expires);
		}
    }
    
    private static void addEnumerationItem(List<Object> itemListAny, 
            EnumerationItem ee, 
            EnumerationModeType mode,
            DocumentBuilder builder,
            XmlBinding binding) throws JAXBException {
        if (mode == null) {
            itemListAny.add(ee.getItem());
        } else if (EnumerationModeType.ENUMERATE_EPR.equals(mode)) {
            itemListAny.add(Addressing.FACTORY.createEndpointReference(ee.getEndpointReference()));
        } else if (EnumerationModeType.ENUMERATE_OBJECT_AND_EPR.equals(mode)) {
            final Document doc = builder.newDocument();
            final Element item =
                    doc.createElementNS(EnumerationExtensions.ITEM.getNamespaceURI(),
                    EnumerationExtensions.WSMAN_ITEM);
            final Document epr =  builder.newDocument();
            binding.marshal(Addressing.FACTORY.
                    createEndpointReference(ee.getEndpointReference()),epr);
        	Object obj = ee.getItem();
            if (obj instanceof Element) {
            	// Already an Element. Import it.
               item.appendChild(doc.importNode((Element)obj, true));
        	} else {
        		// Not an Element, so we meed to marshall it to a Document first.
        		final Document element =  builder.newDocument();
        		binding.marshal(obj, element);
        		item.appendChild(doc.importNode(element.getDocumentElement(), true));
        	}
            item.appendChild(doc.importNode(epr.getDocumentElement(), true));
            itemListAny.add(item);;
        }
    }
    
    public void setPullResponse(final List<EnumerationItem> items, final Object context, final boolean haveMore, EnumerationModeType mode)
    throws JAXBException, SOAPException {
    	
        final ItemListType itemList = FACTORY.createItemListType();
        final List<Object> itemListAny = itemList.getAny();
        final DocumentBuilder builder = getDocumentBuilder();
        final XmlBinding binding = getXmlBinding();
        // go through each element in the list and add appropriate item to list
        // depending on the EnumerationModeType
        for (final EnumerationItem ee : items) {
        	addEnumerationItem(itemListAny,ee,mode,builder,binding);
        }
        
        super.setPullResponse(itemList, context, haveMore);
    }
    
    public List<EnumerationItem> getItems() throws JAXBException, SOAPException {
		final List<Object> items;
		final PullResponse pullResponse = getPullResponse();
		if (pullResponse != null) {
			final ItemListType list = pullResponse.getItems();
			if (list == null) {
				return null;
			}
			items = list.getAny();
		} else {
			final EnumerateResponse enumerateResponse = getEnumerateResponse();
			if (enumerateResponse != null) {
				final Object obj = extract(enumerateResponse.getAny(),
						AnyListType.class, ITEMS);
				if (obj instanceof AnyListType) {
					items = ((AnyListType) obj).getAny();
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
		if (items == null) {
			return null;
		}
		final int size = items.size();
		final List<EnumerationItem> itemList = new ArrayList<EnumerationItem>();
		for (int i = 0; i < size; i++) {
			final Object object = items.get(i);
			itemList.add(unbindItem(object));
		}
		return itemList;
	}
    
    public boolean isEndOfSequence()
    throws JAXBException, SOAPException {
    	final Object eos;
    	final PullResponse pullResponse = getPullResponse();
    	if (pullResponse != null) {
    		eos = pullResponse.getEndOfSequence();
    	} else {
    		final EnumerateResponse enumerateResponse = getEnumerateResponse();
    		if (enumerateResponse != null) {
    			eos = extract(enumerateResponse.getAny(), AttributableEmpty.class, END_OF_SEQUENCE);
    		} else {
    			return false;
    		}
    	}
        return null != eos;
    }
    
    private int getNextElementIndex(NodeList list, int start) {
        for(int i = start; i < list.getLength(); i++) {
            Node n = list.item(i);
            int type = n.getNodeType();
            if(type == Node.ELEMENT_NODE) {
                return i;
            }
        }
        return -1;
    }
    
    private EnumerationItem unbindItem(Object obj)
        throws JAXBException {
        // the three possibilities are: EPR only, Item and EPR or Item only
        
    	Object item = null;
        EndpointReferenceType eprt = null;
		if (obj instanceof JAXBElement) {			
			final JAXBElement elt = (JAXBElement) obj;
			if (EndpointReferenceType.class.equals(elt.getDeclaredType())) {
				// EPR only
				eprt = ((JAXBElement<EndpointReferenceType>) obj).getValue();
			} else {
				if (elt.getName().equals(ITEM)) {
					// The WS Management XSD has been fixed.
					// This is a wsman:item.
					// TODO: Add support when Object factory exists.
					return null;
				} else {
					// JAXBElement
					item = elt;
				}
			}
			return new EnumerationItem(item, eprt);
		} else if (obj instanceof Element) {
            // could be item only or item + EPR
            final Element elt = (Element)obj;
            if (ITEM.getLocalPart().equals(elt.getLocalName()) &&
                    ITEM.getNamespaceURI().equals(elt.getNamespaceURI())) {
                // item + epr wrapped in wsman:Item
                final NodeList list = elt.getChildNodes();
                final int objpos = getNextElementIndex(list,0);
                if (objpos > -1) item = (Element)list.item(objpos);
                final int eprpos = getNextElementIndex(list,objpos+1);
                if (eprpos > -1) {
                    final JAXBElement<EndpointReferenceType> epr =
                        (JAXBElement<EndpointReferenceType>)
                        getXmlBinding().unmarshal(list.item(eprpos));
                    eprt = epr.getValue();
                }
            } else {
                // item only
                item = elt;
            }
            return new EnumerationItem(item, eprt);
        }
        return new EnumerationItem(obj, null);
   }
    
    public void setRequestTotalItemsCountEstimate() throws JAXBException {
        final AttributableEmpty empty = new AttributableEmpty();
        final JAXBElement<AttributableEmpty> emptyElement =
                Management.FACTORY.createRequestTotalItemsCountEstimate(empty);
        getXmlBinding().marshal(emptyElement, getHeader());
    }
    
    public AttributableEmpty getRequestTotalItemsCountEstimate() throws JAXBException, SOAPException {
        final Object value = unbind(getHeader(), REQUEST_TOTAL_ITEMS_COUNT_ESTIMATE);
        return value == null ? null : ((JAXBElement<AttributableEmpty>) value).getValue();
    }
    
    public void setTotalItemsCountEstimate(final BigInteger itemCount) throws JAXBException {
        final AttributableNonNegativeInteger count = new AttributableNonNegativeInteger();
        final JAXBElement<AttributableNonNegativeInteger> countElement =
                Management.FACTORY.createTotalItemsCountEstimate(count);
        if (itemCount == null) {
            /*
             * TODO: does not work yet - bug in JAXB 2.0 FCS, see Issue 217 in JAXB
             * https://jaxb.dev.java.net/issues/show_bug.cgi?id=217
             */
            countElement.setNil(true);
        } else {
            count.setValue(itemCount);
        }
        getXmlBinding().marshal(countElement, getHeader());
    }
    
    public AttributableNonNegativeInteger getTotalItemsCountEstimate() throws JAXBException, SOAPException {
        final Object value = unbind(getHeader(), TOTAL_ITEMS_COUNT_ESTIMATE);
        return value == null ? null : ((JAXBElement<AttributableNonNegativeInteger>) value).getValue();
    }
    
    private static Object extract(final List<Object> anyList, final Class classType, final QName eltName) {
        for (final Object any : anyList) {
            if (any instanceof JAXBElement) {
                final JAXBElement elt = (JAXBElement) any;
                if ((classType != null && classType.equals(elt.getDeclaredType())) &&
                        (eltName != null && eltName.equals(elt.getName()))) {
                    return elt.getValue();
                }
            }
        }
        return null;
    }

	public DialectableMixedDataType getWsmanFilter() throws JAXBException, SOAPException {
		Enumerate enumerate = getEnumerate();
		
		if (enumerate == null) {
			return null;
		}
		return (DialectableMixedDataType) extract(enumerate.getAny(), 
				                                  DialectableMixedDataType.class, 
				                                  FILTER);
	}
	
	public EnumerationModeType getModeType() throws JAXBException, SOAPException {
		Enumerate enumerate = getEnumerate();
		
		if (enumerate == null) {
			return null;
		}
		EnumerationModeType type =  (EnumerationModeType) extract(enumerate.getAny(), 
				                                                  EnumerationModeType.class, 
		                                                          ENUMERATION_MODE);
		return (type == null) ? null : type;
	}
	
	public Mode getMode() throws JAXBException, SOAPException {
		Enumerate enumerate = getEnumerate();
		
		if (enumerate == null) {
			return null;
		}
		EnumerationModeType type =  getModeType();
		return (type == null) ? null : Mode.valueOf(type.value());
	}

	public boolean getOptimize() throws JAXBException, SOAPException {
		Enumerate enumerate = getEnumerate();
		
		if (enumerate == null) {
			return false;
		}
		AttributableEmpty optimize =  (AttributableEmpty) extract(enumerate.getAny(), 
                                               AttributableEmpty.class, 
				                               OPTIMIZE_ENUMERATION);
		return (optimize == null) ? false : true;
	}

	public int getMaxElements() throws JAXBException, SOAPException {
		Enumerate enumerate = getEnumerate();
		
		if (enumerate == null) {
			return 1;
		}
		AttributablePositiveInteger max =  (AttributablePositiveInteger) extract(enumerate.getAny(), 
				AttributablePositiveInteger.class, 
				MAX_ELEMENTS);

		return (max == null) ? 1 : max.getValue().intValue();
	}

	public void setFilterNamespaces(Map<String, String> namespaces)
			throws SOAPException {
		SOAPBody body = getBody();

		// Add the namespaces to the body.
		if (namespaces != null) {
			Set<String> prefixes = namespaces.keySet();
			Iterator prefixIterator = prefixes.iterator();
			while (prefixIterator.hasNext()) {
				String prefix = (String) prefixIterator.next();
				String uri = namespaces.get(prefix);
				body.addNamespaceDeclaration(prefix, uri);
			}
		}
	}
}
