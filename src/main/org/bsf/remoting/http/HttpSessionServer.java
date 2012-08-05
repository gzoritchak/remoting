/**
 * Copyright (c) 2002 Bright Side Factory.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Bright Side Factory (http://www.bs-factory.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Bright Side", "BS Factory" and "Bright Side Factory" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact info@bs-factory.org.
 *
 * 5. Products derived from this software may not be called "Bright Side",
 *    nor may "Bright Side" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Bright Side Factory.  For more
 * information on the Bright Side Factory, please see
 * <http://www.bs-factory.org/>.
 *
 */
package org.bsf.remoting.http;

import org.bsf.remoting.util.naming.PropertiesICFactory;
import org.apache.commons.logging.*;
import org.bsf.remoting.EJBDefinition;
import org.bsf.remoting.http.HttpServiceKey;
import org.bsf.remoting.http.HttpServiceRequest;
import org.bsf.remoting.http.HttpServiceResponse;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Map;
import java.security.Principal;

/**
 * This is the servlet that intercept all the client calls and transmits these to the EJBs.
 * All the client calls are Http POST calls that use url rewriting to keep the session id.
 * <p>
 * If the client uses authentication, it should call an authenticate(String login, String password)
 * on a session.
 * <p>
 * The lookup on EJB are made through the _ejbContext InitialContext. This context
 * is instantiated using the properties
 *
 */
public class HttpSessionServer extends HttpServlet {

    Log log = LogFactory.getLog( HttpSessionServer.class );

    //The cache of remote stateless services (statefull are saved in the user session)
    protected static Hashtable serviceCache = new Hashtable();

    //The index used as key for the statefull services
    private int _maxServiceIndex = 0;

    //The Initial context for EJB lookup
    private static Context _ejbContext;
    private static String _ejbContextProperties = null;


    private static final String STATEFULL_CACHE = "stafullCache";


    /**
     * Uses the properties defined in the servlet environment to instantiate the
     * principalManager and the initial context properties.
     */
    public void init() throws ServletException {
        super.init();


        if ( _ejbContextProperties == null ) {
            _ejbContextProperties = getServletContext()
                    .getInitParameter( "ejbContextProperties" );
        }
        initEjbContext( _ejbContextProperties );

    }

    /**
     * The only http call used by the client side is POST in order to deal with unlimited (??)
     * size of stream.
     */
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {

        //Extract the serviceRequest from the stream
        HttpServiceRequest httpServiceRequest = getHttpServiceRequest( request );
        HttpServiceResponse httpServiceResponse;
        EJBObject remoteRef;


        //Verifiying the session existence
        HttpSession session = request.getSession( false );

        if ( session == null ) {
            //this is the first call, we create the session .
            session = request.getSession( true );

            Principal principal = request.getUserPrincipal();
            String username = null;
            if (principal != null){
                username = principal.getName();
            }
            if (username == null){
                username ="";
            }
            log.info("Creation of a new session for : " + request.getContextPath()
                    + " " + username);
        }

        try {


            //retrieve the service reference
            remoteRef = getEjbReference( httpServiceRequest, request );


            //We can make the remote call
            httpServiceResponse = processRemoteCall( remoteRef, httpServiceRequest, request );

        } catch( NoSuchObjectException e ) {

            //The server may have restart => we clear the cache and the IC
            //and retry
            initEjbContext( _ejbContextProperties );
            serviceCache = new Hashtable();
            remoteRef = getEjbReference( httpServiceRequest, request );
            try {
                httpServiceResponse = processRemoteCall(
                        remoteRef, httpServiceRequest, request );
            } catch( NoSuchObjectException ex ) {
                //There is definitely a problem
                httpServiceResponse = new HttpServiceResponse(
                        new RemoteException( "Impossible to make the remote call."
                                             + "Restart your application." ) );
            }
        }

        //Write the result in the http stream
        response.addHeader( "jsessionid", session.getId() );
        writeHttpServiceResponse( response, httpServiceResponse );
    }

    /**
     * Retrieves the reference to the EJBObject by the definition or by the cache depending
     * on the request.
     */
    private EJBObject getEjbReference( HttpServiceRequest httpServiceRequest, HttpServletRequest request ) {
        EJBObject remoteRef;
        if ( httpServiceRequest.isStateless() )
            remoteRef = getRemote( httpServiceRequest.getRemoteService() );
        else
            remoteRef = getFromCache( httpServiceRequest.getKeyToStatefullService(),
                                      request );
        return remoteRef;
    }


    /**
     */
    private HttpServiceResponse processRemoteCall( EJBObject remoteService,
                                                   HttpServiceRequest httpServiceRequest, HttpServletRequest request )
            throws NoSuchObjectException {

        HttpServiceResponse httpServiceResponse;
        try {
            String p_methodName = httpServiceRequest.getMethodName();
            Class[] paramTypes = httpServiceRequest.getParamTypes();
            Object[] p_args = httpServiceRequest.getArgs();

            HttpServiceKey newServiceKey = null;
            Object remoteResult = null;

            if ( remoteService == null )
                throw new IllegalArgumentException
                        ( "UserSessionBean : The remote must be not null" );

            if ( p_methodName == null )
                throw new IllegalArgumentException
                        ( "UserSessionBean : The invoked method must be not null" );


            Method invokedMethod = remoteService.getClass().getMethod( p_methodName,
                                                                       paramTypes );

            log.debug( "Invoking : " + p_methodName );
            remoteResult = invokedMethod.invoke( remoteService, narrowArgs( p_args ) );

            if ( remoteResult instanceof EJBObject ) {
                //The result of the invoked method is an EJB
                //we create a new kew to store it
                newServiceKey = new HttpServiceKey( _maxServiceIndex++ );

                // put the statefull ref in the session
                putInCache( newServiceKey, (EJBObject) remoteResult, request );

                // and send the key as the result
                remoteResult = newServiceKey;
            }
            httpServiceResponse = new HttpServiceResponse( remoteResult );
        } catch( InvocationTargetException e ) {
            if ( e.getTargetException() instanceof NoSuchObjectException ) {
                NoSuchObjectException noSuchObjectException = (NoSuchObjectException) e.getTargetException();
                throw noSuchObjectException;
            }

            //The target exception is the only result sent back on the client
            httpServiceResponse = new HttpServiceResponse( e.getTargetException() );
        } catch( NoSuchMethodException ex ) {
            throw new RuntimeException( ex.getLocalizedMessage() );
        } catch( IllegalAccessException ex ) {
            throw new RuntimeException( ex.getLocalizedMessage() );
        }
        return httpServiceResponse;
    }

    /**
     * writes the response in the http stream
     */
    private void writeHttpServiceResponse( HttpServletResponse response, HttpServiceResponse httpServiceResponse ) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( outputStream );
        oos.writeObject( httpServiceResponse );
        oos.close();
    }

    /**
     * gets the request from the http stream
     */
    private HttpServiceRequest getHttpServiceRequest( HttpServletRequest request )
            throws IOException {
        //On recupere la requete dans le flux d'entree
        ObjectInputStream ois = new ObjectInputStream(
                request.getInputStream() );
        HttpServiceRequest httpServiceRequest = null;
        try {
            httpServiceRequest = (HttpServiceRequest)
                    ois.readObject();
        } catch( ClassNotFoundException e ) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
        ois.close();
        return httpServiceRequest;
    }


    /**
     * As we invoke the method on the remote service using the reflect package,
     * we must know the classes of the arguments
     */
    private Class[] getParamTypes( Object[] p_args ) {
        int argsLength = 0;
        if ( p_args != null ) {
            argsLength = p_args.length;
        }
        if ( argsLength == 0 )
            return new Class[ 0 ];
        Class[] types = new Class[ argsLength ];
        for ( int i = 0 ; i < argsLength ; i++ ) {
            types[ i ] = p_args[ i ].getClass();
        }
        return types;
    }


    /**
     * Used for a call on a stateless service. The first time this method is called
     * on a service the remote service is created by the invocation of the create method
     * on the EJBHome.
     */
    private EJBObject getRemote( EJBDefinition p_service ) {

        EJBObject result = null;

        Object item = serviceCache.get( p_service.getJndiName() );
        if (item != null)
            result = (EJBObject) PortableRemoteObject.narrow( item, p_service.getRemoteClass() );
        else{
            try {
                EJBHome home = (EJBHome) PortableRemoteObject.narrow(
                        _ejbContext.lookup( p_service.getJndiName() ), p_service.getHomeClass() );
                Method createMethod = home.getClass().getMethod( "create", null );
                result = (EJBObject) createMethod.invoke( home, null );
                serviceCache.put( p_service.getJndiName(), result );
            } catch( Exception ex ) {
                log.fatal( "Error while getting the Home  : " + p_service.getHomeClass() );
                log.fatal( "with the lookup on  : " + p_service.getJndiName(), ex );
            }
        }
        return result;
    }

    /**
     * We put in cache the handles to prepare the passivation
     */
    private void putInCache( HttpServiceKey p_serviceKey, EJBObject p_stub,
                             HttpServletRequest request ) {
        try {
            getStatefulCache(request).put( p_serviceKey, p_stub.getHandle() );
        } catch( RemoteException ex ) {
            log.fatal("Error during the retrieving of the stateful EJB handle." +
                    "\nThe next calls on the EJB will fail!!!", ex);
        }
    }


    /**
     * Retrieve the statefull service from cache
     */
    private EJBObject getFromCache( HttpServiceKey serviceKey, HttpServletRequest request ) {
        EJBObject result = null;
        Class remoteClass = null;
        Handle handle = null;
        try {
            handle = (Handle) getStatefulCache(request).get( serviceKey );
            result = handle.getEJBObject();
            //we don't know the remote class of the EJBObject => we find it with the EJBMetadata
            remoteClass = result.getEJBHome().getEJBMetaData().getRemoteInterfaceClass();
            result = (EJBObject) PortableRemoteObject.narrow( result, remoteClass );
        } catch( Exception ex ) {
            log.fatal(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex.getLocalizedMessage());
        }//should never happen
        return result;
    }

    /**
     * Retrieve the stateful cache from the session. If the stateful cache hasn't been
     * created yet, instantiate it.
     * @param request
     * @return
     */
    private Map getStatefulCache( HttpServletRequest request){
        HttpSession session = request.getSession( false );
        if ( session == null ) {
            log.error( "There is no session, it is not possible"
                       + " to call a statefull service" );
            return null;
        }
        Map statefulCache = (Map) session.getAttribute( STATEFULL_CACHE );
        if (statefulCache == null){
            statefulCache = new Hashtable();
            session.setAttribute( STATEFULL_CACHE, statefulCache );
        }
        return statefulCache;
    }

    /**
     * Allows the session to have remote EJBObject as arguments
     */
    private static Object[] narrowArgs( Object[] p_args ) {
        if ( p_args == null ) return null;
        int length = p_args.length;
        Object[] result = new Object[ length ];
        for ( int i = 0 ; i < length ; i++ ) {
            if ( p_args[ i ] instanceof Remote )
                result[ i ] = PortableRemoteObject.narrow( p_args[ i ], EJBObject.class );
            else
                result[ i ] = p_args[ i ];
        }
        return result;
    }


    private static synchronized void initEjbContext( String jndiProperties ) {

        if ( _ejbContext != null ) {
            _ejbContext = null;

            //Running the GC we realease the weak references used to cache
            // the server references.
            System.gc();
        }
        _ejbContext = PropertiesICFactory.createInitialContext( jndiProperties );
    }

}