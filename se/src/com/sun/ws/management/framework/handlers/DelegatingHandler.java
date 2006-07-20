package com.sun.ws.management.framework.handlers;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.ws.management.Management;
import com.sun.ws.management.enumeration.Enumeration;
import com.sun.ws.management.framework.enumeration.Enumeratable;
import com.sun.ws.management.framework.transfer.Transferable;

/**
 * The Deligating Handler attemps for forward your action request
 * to a deligate class before claiming that it does not support
 * your action.
 * 
 * @author wire
 *
 */
public class DelegatingHandler extends DefaultHandler {
    private static Logger LOG = Logger.getLogger(DelegatingHandler.class.getName());
    protected Object delegate;
    public DelegatingHandler(Object delegate) {
        super();
        setDelegate(delegate);
    }


    @Override
    public void create(String resource, Management request, Management response) {
        if(!(delegate instanceof Transferable)){
            LOG.log(Level.SEVERE,"A call to create on the class "+delegate.getClass().getName()+" failed because it did not implement the Transferable interface.");
            super.create(resource, request, response);
        }
        Transferable transferableDeligate = (Transferable)delegate;
        transferableDeligate.create(request,response);

    }


    @Override
    public void delete(String resource, Management request, Management response) {
        if(!(delegate instanceof Transferable)){
            LOG.log(Level.SEVERE,"A call to delete on the class "+delegate.getClass().getName()+" failed because it did not implement the Transferable interface.");
            super.delete(resource, request, response);
        }
        Transferable transferableDeligate = (Transferable)delegate;
        transferableDeligate.delete(request,response);
    }
    @Override
    public void get(String resource, Management request, Management response) {
        if(!(delegate instanceof Transferable)){
            LOG.log(Level.SEVERE,"A call to get on the class "+delegate.getClass().getName()+" failed because it did not implement the Transferable interface.");
            super.get(resource, request, response);
        }
        Transferable transferableDeligate = (Transferable)delegate;
        transferableDeligate.get(request,response);
    }

    @Override
    public void put(String resource, Management request, Management response) {
        if(!(delegate instanceof Transferable)){
            LOG.log(Level.SEVERE,"A call to put on the class "+delegate.getClass().getName()+" failed because it did not implement the Transferable interface.");
            super.put(resource, request, response);
        }
        Transferable transferableDeligate = (Transferable)delegate;
        transferableDeligate.put(request, response);
    }

    @Override
    public void release(String resource, Enumeration enuRequest, Enumeration enuResponse) {
        if(!(delegate instanceof Enumeratable)){
            LOG.log(Level.SEVERE,"A call to put on the class "+delegate.getClass().getName()+" failed because it did not implement the Enumeratable interface.");
            super.release(resource, enuRequest, enuResponse);
        }
        Enumeratable enumeratableDeligate = (Enumeratable)delegate;
        enumeratableDeligate.release(enuRequest, enuResponse);
    }

    @Override
    public void pull(String resource, Enumeration enuRequest, Enumeration enuResponse) {
        if(!(delegate instanceof Enumeratable)){
            LOG.log(Level.SEVERE,"A call to put on the class "+delegate.getClass().getName()+" failed because it did not implement the Enumeratable interface.");
            super.release(resource, enuRequest, enuResponse);
        }
        Enumeratable enumeratableDeligate = (Enumeratable)delegate;
        enumeratableDeligate.pull(enuRequest, enuResponse);

    }

    @Override
    public void enumerate(String resource, Enumeration enuRequest, Enumeration enuResponse) {
        if(!(delegate instanceof Enumeratable)){
            LOG.log(Level.SEVERE,"A call to put on the class "+delegate.getClass().getName()+" failed because it did not implement the Enumeratable interface.");
            super.release(resource, enuRequest, enuResponse);
        }
        Enumeratable enumeratableDeligate = (Enumeratable)delegate;
        enumeratableDeligate.enumerate(enuRequest, enuResponse);
    }

    @Override
    public void getStatus(String resource, Enumeration enuRequest, Enumeration enuResponse) {
         if(!(delegate instanceof Enumeratable)){
            LOG.log(Level.SEVERE,"A call to put on the class "+delegate.getClass().getName()+" failed because it did not implement the Enumeratable interface.");
            super.release(resource, enuRequest, enuResponse);
        }
        Enumeratable enumeratableDeligate = (Enumeratable)delegate;
        enumeratableDeligate.getStatus(enuRequest, enuResponse);
    }

    @Override
    public void renew(String resource, Enumeration enuRequest, Enumeration enuResponse) {
        if(!(delegate instanceof Enumeratable)){
            LOG.log(Level.SEVERE,"A call to put on the class "+delegate.getClass().getName()+" failed because it did not implement the Enumeratable interface.");
            super.release(resource, enuRequest, enuResponse);
        }
        Enumeratable enumeratableDeligate = (Enumeratable)delegate;
        enumeratableDeligate.renew(enuRequest, enuResponse);
    }

    /**
     * Attempts to call a custom action based on introspection of the deligate.
     * Assumes the last part of the action URI maps to the method name on the delegate
     * class in lower case.
     */
    @Override
    public boolean customDispatch(String action, String resource, Management request, Management response) throws Exception {
        String[] actionParts = action.split("/");
        if(actionParts.length==0)
            return false;

        String methodName=actionParts[actionParts.length-1].toLowerCase();
        Method method=null;
        try {
            method=delegate.getClass().getMethod(methodName,Management.class,Management.class);
            method.invoke(delegate,request,response);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"A call to a custom method \""+methodName+"\" on the class "+delegate.getClass().getName()+" failed because of this error:"+ e.getMessage());
            return false;
        }

    }

    public void setDelegate(Object delegate){
        this.delegate=delegate;
    }

}
