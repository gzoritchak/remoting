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

import org.bsf.remoting.ServiceFactory;
import org.bsf.remoting.EJBDefinition;

import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.Map;


/**
 * This implementation of ServiceFactory makes returns to the client DynamicProxies to
 * encapsulate the client calls in http protocol. The client sees its "normal" remote interface.
 * @version@
 */
public class HttpServiceFactory implements ServiceFactory {

    private static Map _serviceCache = new Hashtable();


    public HttpServiceFactory() {
        super();
    }

    public HttpServiceFactory(String host, int port, String serverContext) {
        this(null, host, port, serverContext);
    }

    public HttpServiceFactory(String protocole, String host, int port, String serverContext) {
        super();
        this.setProtocol(protocole);
        this.setHost(host);
        this.setPort(port);
        this.setServerContext(serverContext);
    }

    /**
     * Uses the ejbDefinition to return a DynamicProxy that will present the
     * "good face" to the client.
     */
    public Object getService(EJBDefinition p_service) {

        HttpServiceInvocationHandler service = null;
        Object dynamicProxy = null;

        dynamicProxy = _serviceCache.get(p_service);
        if (dynamicProxy == null) {
            service = new HttpServiceInvocationHandler(p_service);
            dynamicProxy = Proxy.newProxyInstance(HttpServiceInvocationHandler.class.getClassLoader(),
                    new Class[]{p_service.getRemoteClass()}, service);
            _serviceCache.put(p_service, dynamicProxy);
        }
        return dynamicProxy;
    }

    public void setPort(int port) {
        HttpSessionClient.getInstance().setPort(port);
    }

    public void setHost(String host) {
        HttpSessionClient.getInstance().setHost(host);
    }

    public void setProtocol(String protocole){
        HttpSessionClient.getInstance().setProtocol(protocole);
    }


    /**
     * @deprecated Use setServerContext() instead.
     * @param file
     */
    public void setServerFile(String file) {
        HttpSessionClient.getInstance().setServerFile(file);
    }

    /**
     * Set the context used at deployment time. For instance if the packaging of
     * the remoting war inside of the ear use the context myApp, you should call
     * setContext("myApp") on the HttpServiceFactory to reach the server.
     * @param context
     */
    public void setServerContext(String context){
        HttpSessionClient.getInstance().setContext(context);

    }

    public void setThreadCount(int count) {
        HttpSessionClient.getInstance().setThreadCount(count);
    }

    public void setLogin(String login) {
        HttpSessionClient.getInstance().setLogin(login);
    }

    public void setPassword(String password){
        HttpSessionClient.getInstance().setPassword(password);
    }
}