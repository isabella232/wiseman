
package com.hp.itil._2007._05.servicetype;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.hp.itil._2007._05.servicetype package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ServiceDefinition_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "ServiceDefinition");
    private final static QName _FormalParameterList_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "FormalParameterList");
    private final static QName _ServiceTypeNameId_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "ServiceTypeNameId");
    private final static QName _ServiceTypeACL_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "ServiceTypeACL");
    private final static QName _ServiceTypeModel_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "ServiceTypeModel");
    private final static QName _Type_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "Type");
    private final static QName _ServiceTypeMetadata_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "ServiceTypeMetadata");
    private final static QName _Version_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "Version");
    private final static QName _RollbackPolicy_QNAME = new QName("http://www.hp.com/itil/2007/05/ServiceType", "RollbackPolicy");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.hp.itil._2007._05.servicetype
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ServiceTypeACLType }
     * 
     */
    public ServiceTypeACLType createServiceTypeACLType() {
        return new ServiceTypeACLType();
    }

    /**
     * Create an instance of {@link RollbackPolicyType }
     * 
     */
    public RollbackPolicyType createRollbackPolicyType() {
        return new RollbackPolicyType();
    }

    /**
     * Create an instance of {@link FormalParameterType }
     * 
     */
    public FormalParameterType createFormalParameterType() {
        return new FormalParameterType();
    }

    /**
     * Create an instance of {@link ServiceTypeType }
     * 
     */
    public ServiceTypeType createServiceTypeType() {
        return new ServiceTypeType();
    }

    /**
     * Create an instance of {@link ServiceTypeMetadataType }
     * 
     */
    public ServiceTypeMetadataType createServiceTypeMetadataType() {
        return new ServiceTypeMetadataType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "ServiceDefinition")
    public JAXBElement<Object> createServiceDefinition(Object value) {
        return new JAXBElement<Object>(_ServiceDefinition_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FormalParameterType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "FormalParameterList")
    public JAXBElement<FormalParameterType> createFormalParameterList(FormalParameterType value) {
        return new JAXBElement<FormalParameterType>(_FormalParameterList_QNAME, FormalParameterType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "ServiceTypeNameId")
    public JAXBElement<String> createServiceTypeNameId(String value) {
        return new JAXBElement<String>(_ServiceTypeNameId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceTypeACLType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "ServiceTypeACL")
    public JAXBElement<ServiceTypeACLType> createServiceTypeACL(ServiceTypeACLType value) {
        return new JAXBElement<ServiceTypeACLType>(_ServiceTypeACL_QNAME, ServiceTypeACLType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceTypeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "ServiceTypeModel")
    public JAXBElement<ServiceTypeType> createServiceTypeModel(ServiceTypeType value) {
        return new JAXBElement<ServiceTypeType>(_ServiceTypeModel_QNAME, ServiceTypeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OperationModeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "Type")
    public JAXBElement<OperationModeType> createType(OperationModeType value) {
        return new JAXBElement<OperationModeType>(_Type_QNAME, OperationModeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceTypeMetadataType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "ServiceTypeMetadata")
    public JAXBElement<ServiceTypeMetadataType> createServiceTypeMetadata(ServiceTypeMetadataType value) {
        return new JAXBElement<ServiceTypeMetadataType>(_ServiceTypeMetadata_QNAME, ServiceTypeMetadataType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "Version")
    public JAXBElement<String> createVersion(String value) {
        return new JAXBElement<String>(_Version_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RollbackPolicyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.hp.com/itil/2007/05/ServiceType", name = "RollbackPolicy")
    public JAXBElement<RollbackPolicyType> createRollbackPolicy(RollbackPolicyType value) {
        return new JAXBElement<RollbackPolicyType>(_RollbackPolicy_QNAME, RollbackPolicyType.class, null, value);
    }

}
