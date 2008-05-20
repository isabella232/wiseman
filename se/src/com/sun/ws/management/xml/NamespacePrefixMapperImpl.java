package com.sun.ws.management.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.ws.management.Management;
import com.sun.ws.management.addressing.Addressing;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.eventing.Eventing;
import com.sun.ws.management.server.WSManAgent;
import com.sun.ws.management.soap.SOAP;
import com.sun.ws.management.transfer.Transfer;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

	private static Map<String, String> extensionNamespaces = WSManAgent
			.locateExtensionNamespaces();

	private final Map<String, String> mappings = new HashMap<String, String>();
	private final Set<String> predeclaredUris = new HashSet<String>();

	public NamespacePrefixMapperImpl(final Map<String, String> mappings) {

		// Add the default WS Management namespaces
		addPreDeclaredNamespace(XMLSchema.NS_URI, XMLSchema.NS_PREFIX);
		addPreDeclaredNamespace(SOAP.NS_URI, SOAP.NS_PREFIX);
		addPreDeclaredNamespace(Addressing.NS_URI, Addressing.NS_PREFIX);
		addPreDeclaredNamespace(Eventing.NS_URI, Eventing.NS_PREFIX);
		addPreDeclaredNamespace(Enumeration.NS_URI, Enumeration.NS_PREFIX);
		addPreDeclaredNamespace(Transfer.NS_URI, Transfer.NS_PREFIX);
		addPreDeclaredNamespace(Management.NS_URI, Management.NS_PREFIX);

		// Add any extension namespaces
		if ((extensionNamespaces != null) && (extensionNamespaces.size() > 0)) {
			for (final String prefix : extensionNamespaces.keySet()) {
				addPreDeclaredNamespace(extensionNamespaces.get(prefix).trim(),
						prefix.trim());
			}
		}

		// Add any namespaces requested by the caller
		if ((mappings != null) && (mappings.size() > 0)) {
			for (final String uri : mappings.keySet()) {
				addPreDeclaredNamespace(uri.trim(), mappings.get(uri));
			}
		}
	}

	public String getPreferredPrefix(final String namespaceUri,
			final String suggestion, final boolean requirePrefix) {
		final String prefix = mappings.get(namespaceUri);

		if (prefix != null)
			return prefix;
		else
			return suggestion;
	}

	public String[] getPreDeclaredNamespaceUris() {
		final String[] array = new String[predeclaredUris.size()];

		return predeclaredUris.toArray(array);
	}

	public void addPreDeclaredNamespace(final String uri, final String prefix) {
		this.mappings.put(uri, prefix);
		this.predeclaredUris.add(uri);
	}
}
