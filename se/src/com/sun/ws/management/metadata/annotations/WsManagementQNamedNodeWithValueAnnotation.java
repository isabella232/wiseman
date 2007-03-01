package com.sun.ws.management.metadata.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/** This annotation provides a mechanism of defining a cutom
 *  QNamed node with a simple text value.
 * 
 * Ex. <id:SpecVersion>1.0.0a</id:SpecVersion>
 */
@Retention(RUNTIME)
@Target(value=ANNOTATION_TYPE)
public @interface WsManagementQNamedNodeWithValueAnnotation {
	
	/** The value of the namespace uri for the QName of the node
	 * 
	 * Ex: namespaceURI="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd"
	 */
	public String namespaceURI() default "";

	/** The value of the localpart for the QName of the node
	 * 
	 * Ex: localpart="ResourceURI"
	 */
	public String localpart() default "";

	/** The value of the prefix for the QName of the node
	 * 
	 * Ex: prefix="wsman"
	 */
	public String prefix() default "";

	/** The value to be inserted into the node
	 * 
	 * Ex: nodeValue="http://acme.org/hardware/2005/02/storage/physDisk"
	 */
	public String nodeValue() default "";
	
}