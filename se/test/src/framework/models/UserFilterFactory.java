package framework.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.ws.management.eventing.InvalidMessageFault;
import com.sun.ws.management.server.Filter;
import com.sun.ws.management.server.FilterFactory;
import com.sun.ws.management.server.NamespaceMap;
import com.sun.ws.management.soap.FaultException;

/**
 * @author denis
 *
 */
public class UserFilterFactory implements FilterFactory {
	
	public static final String DIALECT = "http://examples.hp.com/ws/wsman/user/filter/custom";
	
	// Private classes to implement a custom filter
	private class UserNodeList implements NodeList {

		final private ArrayList<Node> list;

		protected UserNodeList() {
			list = new ArrayList<Node>();
		}
		
		protected UserNodeList(int size) {
			list = new ArrayList<Node>(size);
		}
		
		public int getLength() {
			return list.size();
		}

		public Node item(int index) {
			return list.get(index);
		}
		
		protected void add(Node node) {
			list.add(node);
		}
	}
	
    private class UserLastnameFilter implements Filter {
    	
    	private final ArrayList<String> filterLastnames;
    	
    	UserLastnameFilter(List filterExpressions) {
            if ((filterExpressions == null) || (filterExpressions.size() == 0)) {
				throw new InvalidMessageFault("Missing a filter expression");
			}
            final Iterator iter = filterExpressions.iterator();
            filterLastnames = new ArrayList<String>(filterExpressions.size());
            
            // Check the filters
            String expression = "";
            
            while (iter.hasNext()) {
				final Object expr = iter.next();
				if (expr instanceof String) {
					expression = (String)expr;
				} else if (expr instanceof Node) {
					expression = ((Node)expr).getTextContent();
				} else {
					throw new InvalidMessageFault(
							"Invalid filter expression type: " + expr.getClass().getName());
				}
				
				filterLastnames.add(expression);
			}

    	}
    	
        public NodeList evaluate(final Node content) throws FaultException {
        	final NodeList children = content.getChildNodes();

        	for (int i = 0; i < children.getLength(); i++) {
        		Node child = children.item(i);

        		if (child.getLocalName().equals("lastname")) {
        			// Check against the list
        			final Iterator iter = filterLastnames.iterator();
        			
        			while (iter.hasNext()) {
        				String name = (String)iter.next();
        				if (child.getTextContent().equals(name)) {
        					final UserNodeList list = new UserNodeList(1);
        					list.add(content);
        					return list;
        				}
        			}
        		}
        	}
            return new UserNodeList();
        }

		public String getDialect() {
			return DIALECT;
		}

		public Object getExpression() {
			return filterLastnames.get(0);
		}
    }
    
    // Public methods
    public Filter newFilter(List content, 
            NamespaceMap namespaces) throws FaultException, Exception {
        return new UserLastnameFilter(content);
    } 
}
