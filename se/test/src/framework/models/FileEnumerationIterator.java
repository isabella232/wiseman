package framework.models;

import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.NamespaceMap;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * The class to be presented by a data source that would like to be
 * enumerated.
 *
 * Implementations of this class are specific to the data structure being
 * enumerated.
 *
 * @see EnumerationIterator
 */
public class FileEnumerationIterator implements EnumerationIterator {
    private boolean m_cancelled = false;
    
    /**
     * Supply the next few elements of the iteration. This is invoked to
     * satisfy a {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
     * request. The operation must return within the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
     * specified in the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request,
     * otherwise {@link #cancel cancel} will
     * be invoked and the current thread interrupted. When cancelled,
     * the implementation can return the results currently
     * accumulated (in which case no
     * {@link com.sun.ws.management.soap.Fault Fault} is generated) or it can
     * return {@code null} in which case a
     * {@link com.sun.ws.management.enumeration.TimedOutFault TimedOutFault}
     * is returned.
     *
     * @param db A document builder that can be used to create documents into
     * which the returned items will be placed. Note that each item must be
     * placed as the root element of a new Document for XPath filtering to work
     * properly.
     *
     * @param context The client context that was specified to
     * {@link com.sun.ws.management.server.EnumerationSupport#enumerate enumerate} is returned.
     *
     * @param includeItem Indicates whether items are desired, as specified by
     * the EnumerationMode in the Enumerate request.
     *
     * @param includeEPR Indicates whether EPRs are desired, as specified by
     * the EnumerationMode in the Enumerate request.
     *
     * @param startPos The starting position (cursor) for this
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @param count The number of items desired in this
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @return a List of {@link org.w3c.dom.Element Elements} that will be
     * returned in the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.PullResponse PullResponse}.
     */
    
    public List<EnumerationItem> next(final DocumentBuilder db, final Object context,
            final boolean includeItem, final boolean includeEPR,
            final int startPos, final int count) {
        m_cancelled = false;
        final File[] props = (File[]) context;
        final int returnCount = Math.min(count, props.length - startPos);
        final List<EnumerationItem> items = new ArrayList<EnumerationItem>(returnCount);
        
        for (int i = 0; i < returnCount && !m_cancelled; i++) {
            final File value = props[startPos + i];
            final Document doc = db.newDocument();
            final Element item = doc.createElementNS(FileEnumerationHandler.NS_URI, FileEnumerationHandler.NS_PREFIX + ":File");
            item.appendChild(doc.createTextNode(value.getAbsolutePath()));
            EnumerationItem ee = new EnumerationItem(item, null);
            // TODO: fix to add EPR
            items.add(ee);
        }
        return items;
    }
    
    /**
     * Indicates if there are more elements remaining in the iteration.
     *
     * @param context The client context that was specified to
     * {@link com.sun.ws.management.server.EnumerationSupport#enumerate enumerate} is returned.
     *
     * @param startPos The starting position (cursor) for this
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull} request.
     *
     * @return {@code true} if there are more elements in the iteration,
     * {@code false} otherwise.
     */
    public boolean hasNext(final Object context, final int startPos) {
        return startPos < ((File[]) context).length;
    }
    
    /**
     * Invoked when a {@link #next next} call exceeds the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull#getMaxTime timeout}
     * specified in the
     * {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
     * request. An implementation is expected to set a flag that
     * causes the currently-executing {@link #next next} operation to return
     * gracefully.
     *
     * @param context The client context that was specified to
     * {@link com.sun.ws.management.server.EnumerationSupport#enumerate enumerate} is returned.
     */
    public void cancel(final Object context) {
        m_cancelled = true;
    }
    
    public int estimateTotalItems(Object context) {
        final File[] props = (File[]) context;
        return props.length;
    }
    
    public NamespaceMap getNamespaces() {
        return null;
    }
}
