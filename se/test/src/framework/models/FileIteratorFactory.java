
package framework.models;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.ws.management.UnsupportedFeatureFault;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.server.EnumerationItem;
import com.sun.ws.management.server.EnumerationIterator;
import com.sun.ws.management.server.HandlerContext;
import com.sun.ws.management.server.IteratorFactory;
import com.sun.ws.management.soap.FaultException;

/* This enumeration factory is for the "wsman:auth/user" resource.
 * It handles creation of an iterator for use to access the "user" resources.
 * @see com.sun.ws.management.server.IteratorFactory#newIterator(com.sun.ws.management.server.HandlerContext, com.sun.ws.management.enumeration.Enumeration, javax.xml.parsers.DocumentBuilder, boolean, boolean)
 *
 * @author denis
 */
public class FileIteratorFactory implements IteratorFactory {
	
    public static final String NS_URI = "http://files.lookup.com";
    public static final String NS_PREFIX = "fl";

	/* This creates iterators for the "wsman:auth/user" resource.
	 * 
	 * @see com.sun.ws.management.server.IteratorFactory#newIterator(com.sun.ws.management.server.HandlerContext, 
	 * com.sun.ws.management.enumeration.Enumeration, 
	 * javax.xml.parsers.DocumentBuilder, boolean, boolean)
	 */
	public EnumerationIterator newIterator(HandlerContext context,
			Enumeration request, DocumentBuilder db, boolean includeItem,
			boolean includeEPR) throws UnsupportedFeatureFault, FaultException {
		if (includeEPR) {
			throw new UnsupportedFeatureFault(
					UnsupportedFeatureFault.Detail.ENUMERATION_MODE);
		}
		File dirs = new File(System.getProperty("user.dir"));
		if (dirs.exists()) {
			return new FileEnumerationIterator(dirs.listFiles(), db);
		} else {
			return null;
		}
	}
	
	// The Iterator implementation follows
	
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
		
		private DocumentBuilder db;
	    private File[] dirs;
	    int cursor = 0;
	    
	    protected FileEnumerationIterator(File[] dirs, DocumentBuilder db) {
	    	this.db = db;
	    	this.dirs = dirs;
	    }
	    /**
	     * Supply the next few elements of the iteration. This is invoked to
	     * satisfy a {@link org.xmlsoap.schemas.ws._2004._09.enumeration.Pull Pull}
	     * request.
	     *
	     * @return an EnumerationItem.
	     */
	    
	    public EnumerationItem next() {
	        final File[] props = this.dirs; // (File[]) context;
	        
	            final File value = props[cursor];
	            final Document doc = db.newDocument();
	            final Element item = doc.createElementNS(NS_URI, NS_PREFIX + ":File");
	            item.appendChild(doc.createTextNode(value.getAbsolutePath()));
	            EnumerationItem ee = new EnumerationItem(item, null);
	            
	            // TODO: enhance to add EPR
	            cursor++;
	            return ee;
	    }
	    
	    /**
	     * Indicates if there are more elements remaining in the iteration.
	     * 
	     * @return {@code true} if there are more elements in the iteration,
	     * {@code false} otherwise.
	     */
	    public boolean hasNext() {
	        return cursor < dirs.length; // ((File[]) context).length;
	    }
	    
	    /**
	     * Indicates if the iterator has already been filtered.
	     * This indicates that further filtering is not required
	     * by the framwork.
	     *
	     * @return {@code true} if the iterator has already been filtered,
	     * {@code false} otherwise.
	     */
		public boolean isFiltered(Object context) {
			return false;
		}
	    
	    /**
	     * Invoked to indicate the iterator is no longer needed.
	     */
	    public void release() {
	    	dirs = new File[0];
	    }
	    
	    public int estimateTotalItems() {
	        return dirs.length;
	    }
	    
		public boolean isFiltered() {
			return false;
		}
	}
}
