/**
 *Copyright (c) 2002 Bright Side Factory.  All rights
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

import org.apache.commons.logging.*;
import org.apache.commons.codec.binary.Base64;
import org.bsf.remoting.EJBDefinition;
import org.bsf.remoting.http.HttpServiceInvocationHandler;
import org.bsf.remoting.http.HttpServiceKey;
import org.bsf.remoting.http.HttpServiceRequest;
import org.bsf.remoting.http.HttpServiceResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * This class handle the http protocole for the application server communication.
 * Client classes can make http calls in parallel threads. The number of thread
 * is limited by the field maxThreadCount.
 * The parameters of the communication (port, host, ...)can be set by the ressource
 * BsfModules.xml.
 */
public class HttpSessionClient {

    private static Log log = LogFactory.getLog( HttpSessionClient.class );

    private static int requestNb;
    private String sessionId;

    private static final String AUTHENTICATED_SERVLET = "/authenticatedHttpSession";
    private static final String UNAUTHENTICATED_SERVLET = "/httpSession";

    /** Default call method is http*/
    private static final String DEFAULT_PROTOCOL = "http";

    /** Default Server Host*/
    private static final String SERVER_HOST = "localHost";

    /** Default Server Port*/
    private static final int SERVER_PORT = 8080;

    /** Default Server File*/
    private static final String SERVER_CONTEXT = "remoting";

    /** Default Server File*/
    private static final int DEFAULT_THREAD_COUNT = 1;

    /** BSF default mime type */
    private static final String MIME_TYPE = "application/x-bsf";



    /** The method used to transmit the call, the default is http but it can be https
     * to secure the communication
     */
    private String protocol;

    /** Where is the server */
    private String host;

    /**
     * The context used at deployment time.
     */
    private String context;


    /** Used for authentification */
    private String login;

    /** password used for basic authentification */
    private String pass;

    /** Http Port used */
    private int port = -1;

    /** The number of parallel thread used to perform the http call. */
    private int maxThreadCount = DEFAULT_THREAD_COUNT;

    /** The number of thread that are currently making a call */
    private int curUsedThread = 0;

    // Singleton attribute
    protected static HttpSessionClient _instance = null;

    /**
     * Default constructor
     */
    protected HttpSessionClient() {
        super();
        log.debug( "Session client created." );

    }

    /**
     * Invokes a stateless remote service
     */
    public Object invoke( EJBDefinition p_service, Method m, Object[] args )
            throws Throwable {
        Object result = null;

        HttpServiceRequest request = new HttpServiceRequest(
                p_service, m.getName(), m.getParameterTypes(), args );

        //Making the http call
        result = invokeHttp( request );

        if ( result instanceof HttpServiceKey ) {
            //The return type of the method was an ejb, we return to
            // the client a new dynamic proxy
            HttpServiceInvocationHandler service = new HttpServiceInvocationHandler( (HttpServiceKey) result );
            try {
                result = Proxy.newProxyInstance( this.getClass().getClassLoader(),
                                                 new Class[]{m.getReturnType()}, service );
            } catch( IllegalArgumentException e ) {
                log.fatal( e.getLocalizedMessage(), e );
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }

        return result;
    }


    /**
     * Invokes a statefull remote service
     */
    public Object invoke( HttpServiceKey p_servicekey, Method m, Object[] args )
            throws Throwable {
        Object result = null;

        HttpServiceRequest request = new HttpServiceRequest(
                p_servicekey, m.getName(), m.getParameterTypes(), args );

        result = invokeHttp( request );

        //If the return type of the method was an ejb,
        //we return to the client a new dynamic proxy
        if ( result instanceof HttpServiceKey ) {
            result = new HttpServiceInvocationHandler( (HttpServiceKey) result );
        }
        return result;
    }


    /**
     * Performs the http call.
     */
    private Object invokeHttp( HttpServiceRequest request ) throws Throwable {

        String file = null;

        if (isAuthenticatedCall()){
            file = getAuthenticatedServerFile();
        } else {
            file = getUnauthenticatedServerFile();
        }

        int port = ( this.port == -1 ) ? SERVER_PORT : this.port;
        String host = ( this.host == null ) ? SERVER_HOST : this.host;
        String protocol = (this.protocol == null ) ? DEFAULT_PROTOCOL : this.protocol;

        try {
            getThreadLock();

            int currentRequestNb = requestNb++;
            log.debug( "Start remote call " + currentRequestNb + " " + request.getMethodName() );

            HttpServiceResponse httpResponse;

            //A session exists in the server we use URL rewriting to pass the session id
            if ( sessionId != null ) {
                StringBuffer sb = new StringBuffer();
                sb.append( file.toString() );
                sb.append( ";jsessionid=" );
                sb.append( sessionId );
                file = sb.toString();
            }

            URL url = new URL( protocol, host, port, file );

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod( "POST" );
            httpURLConnection.setDoOutput( true );
            httpURLConnection.setDoInput( true );
            httpURLConnection.setUseCaches( false );
            // Set the content type to be application/x-bsf
            httpURLConnection.setRequestProperty( "Content-Type", MIME_TYPE );

            //If we have a login and a password => we can perform a basic
            //authentification
            if (isAuthenticatedCall()){

                String loginAndPass = login + ':' + pass;
                httpURLConnection.setRequestProperty("Authorization",
                        "Basic "
                        + new String (Base64.encodeBase64(loginAndPass.getBytes())));
            }

            ObjectOutputStream oos = new ObjectOutputStream(
                    httpURLConnection.getOutputStream() );
            oos.writeObject( request );
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(
                    httpURLConnection.getInputStream() );
            httpResponse = (HttpServiceResponse) ois.readObject();
            sessionId = httpURLConnection.getHeaderField( "jsessionid" );
            ois.close();
            httpURLConnection.disconnect();

            log.debug( "Ending remote call " + currentRequestNb );


            if ( httpResponse.isExceptionThrown() )
                throw httpResponse.getThrowable();
            return httpResponse.getResult();

        } catch( java.io.IOException e ) {
            if (e instanceof RemoteException){
                throw e;
            }else{
                String message = "Failure during the http remote call on http://"
                        + host + ":" + port + file;
                log.fatal( message, e );
                throw new RemoteException( message, e );
            }
        } catch( ClassNotFoundException e ) {
            String message = "Failure during the http remote call";
            log.fatal( message, e );
            throw new RemoteException( message, e );
        }finally{
            releaseThreadLock();
        }
    }



    /**
     * This method is used to limit the concurrent http call to the max
     * fixed by maxThreadCount and to wait the end of the first call that
     * will return the session id.
     */
    private synchronized void getThreadLock() {
        // We wait the return of the first call
        while ( sessionId == null && curUsedThread > 0 ) {
            try {
                log.debug( "No session. Only one thread is authorized. Waiting ..." );
                wait();
            } catch( InterruptedException e ) {
                log.fatal(e);
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }

        while ( curUsedThread >= maxThreadCount ) {
            try {
                log.debug( "Max concurent http call reached. Waiting ..." );
                wait();
            } catch( InterruptedException e ) {
                log.fatal(e);
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }
        curUsedThread++;
    }

    private synchronized void releaseThreadLock() {
        curUsedThread--;

        notify();
    }


    /**
     * Singleton instanciation
     */
    public static HttpSessionClient getInstance() throws IllegalStateException {
        if ( _instance == null ) {
            _instance = new HttpSessionClient();
        }
        return _instance;
    }

    private boolean isAuthenticatedCall() {
        return login != null && pass != null;
    }


    public void setHost( String host ) {
        this.host = host;
    }

    /**
     * sets the used protocol. The default one is http but you can set it to
     * https to use a secure communication.
     * @param protocol
     */
    public void setProtocol( String protocol) {
        this.protocol = protocol;
    }

    /**
     * @deprecated setServerContext should be used instead.
     * @param serverFile the deployment context and the servlet. For instance /myApp/httpSession.
     */
    public void setServerFile( String serverFile ) {
        if ( ! serverFile.endsWith("httpSession")){
            throw new RuntimeException("You must use the method setContext() instead" +
                    " of the method setServerFile to define your remote call.");
        }
        String context = serverFile.substring(0, serverFile.length() - 11);
        setContext(context);
    }

    /**
     * Set the context used at deployment time. For instance if the packaging of
     * the remoting war inside of the ear use the context myApp, you should call
     * setContext("myApp") on the HttpServiceFactory to reach the server.
     *
     * @param context
     */
    public void setContext(String context) {
        while (context.endsWith("/")){
            context = context.substring(0,context.length() -1 );
        }
        this.context = context;
        log.debug( "Server context is " + context);
    }

    /**
     * @return the server file to call for a unauthenticated call
     */
    private String getUnauthenticatedServerFile(){
        StringBuffer sb = new StringBuffer();
        sb.append('/');
        if (context != null){
            sb.append(context);
        }else{
            sb.append(SERVER_CONTEXT);
        }
        sb.append(UNAUTHENTICATED_SERVLET);
        return sb.toString();
    }

    /**
     * @return the server file to call for an authenticated call
     */
    private String getAuthenticatedServerFile(){
        StringBuffer sb = new StringBuffer();
        sb.append('/');
        if (context != null){
            sb.append(context);
        }else{
            sb.append(SERVER_CONTEXT);
        }
        sb.append(AUTHENTICATED_SERVLET);
        return sb.toString();
    }

    public void setPort( int port ) {
        this.port = port;
    }

    public int getThreadCount() {
        return maxThreadCount;
    }

    public void setThreadCount( int threadCount ) {
        this.maxThreadCount = threadCount;

        log.debug( "Max concurrent thread set to " + threadCount );
    }


    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String pass) {
        this.pass = pass;
    }
}