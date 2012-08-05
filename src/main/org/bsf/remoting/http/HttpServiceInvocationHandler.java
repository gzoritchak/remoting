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


import org.bsf.remoting.EJBDefinition;

import javax.ejb.EJBObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * The HttpServiceInvocationHandler is used to transmit the user request to the correct object which can
 * be the remote service in case of direct connection or the user session client
 * in case of Session connection. <p>
 * This class is a dynamic proxy created by the HttpServiceFactory in case of
 * stateless service or by the HttpSessionClient in case of statefull service.
 * @author Gaetan Zoritchak
 * @version 1.0
 */

public class HttpServiceInvocationHandler implements InvocationHandler {

    private Object _remote = null;
    private EJBDefinition _service = null;
    private HttpServiceKey _serviceKey = null;

    private int _connectionMode;

    /**
     * This constructor is used for a stateless service
     */
    public HttpServiceInvocationHandler(EJBDefinition p_service) {
        _service = p_service;
    }

    /**
     * This constructor is used for a statefull service
     */
    public HttpServiceInvocationHandler(HttpServiceKey p_serviceKey) {
        _serviceKey = p_serviceKey;
    }

    /**
     * This method is called on every call on the interface
     */
    public Object invoke(Object p_proxy, Method p_method, Object[] p_args) throws Throwable {
        Object result = null;
        EJBObject ejbResult = null;
        if (_serviceKey != null) {
            return HttpSessionClient.getInstance().invoke(_serviceKey, p_method, p_args);
        } else {
            return HttpSessionClient.getInstance().invoke(_service, p_method, p_args);
        }
    }
}