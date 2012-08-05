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
import org.bsf.remoting.http.HttpServiceKey;

import java.io.Serializable;


/**
 * This class encapsulates the items of the client request.
 * The service called can be an EJBDefinition (@see org.bsf.framework.treatment.EJBDefinition)
 * corresponding to a Stateless EJB or a key to a service  (@see org.bsf.framework.client.session.HttpServiceKey)
 * when the client interacts with a Stateful Bean
 */
public class HttpServiceRequest implements Serializable {

    private EJBDefinition remoteService;
    private String methodName;
    private String[] paramTypesName;
    private Object[] args;
    private HttpServiceKey keyToStatefullService;

    /**
     * Constructor when using a stateless service
     * @param remoteService
     * @param methodToCall
     * @param paramTypes
     * @param args
     */
    public HttpServiceRequest(EJBDefinition remoteService, String methodToCall,
                              Class[] paramTypes, Object[] args) {
        this.remoteService = remoteService;
        this.methodName = methodToCall;
        setParamTypes(paramTypes);
        this.args = args;
    }

    /**
     * Constructor for a statefull service
     * @param keyToStatefullService
     * @param methodToCall
     * @param paramTypes
     * @param args
     */
    public HttpServiceRequest(HttpServiceKey keyToStatefullService, String methodToCall,
                              Class[] paramTypes, Object[] args) {
        this.keyToStatefullService = keyToStatefullService;
        this.methodName = methodToCall;
        setParamTypes(paramTypes);
        this.args = args;
    }

    public boolean isStatefull() {
        if (keyToStatefullService != null) return true;
        return false;
    }

    public boolean isStateless() {
        if (isStatefull()) return false;
        return true;
    }

    public EJBDefinition getRemoteService() {
        return remoteService;
    }

    public void setRemoteService(EJBDefinition remoteService) {
        this.remoteService = remoteService;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @return The array of params of the invoked method.
     */
    public Class[] getParamTypes() {
        ClassLoader curClassLoader = this.getClass().getClassLoader();
        Class[] result = new Class[paramTypesName.length];
        for (int i = 0; i < paramTypesName.length; i++) {
            String type = paramTypesName[i];
            Class arg = null;
            if (type == null)
                arg = null;
            else if (type.equals("int"))
                arg = Integer.TYPE;
            else if (type.equals("boolean"))
                arg = Boolean.TYPE;
            else if (type.equals("float"))
                arg = Float.TYPE;
            else if (type.equals("byte"))
                arg = Byte.TYPE;
            else if (type.equals("short"))
                arg = Short.TYPE;
            else if (type.equals("char"))
                arg = Character.TYPE;
            else if (type.equals("long"))
                arg = Long.TYPE;
            else if (type.equals("double"))
                arg = Double.TYPE;
            else
                try {
                    arg = Class.forName(type);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e.getLocalizedMessage());
                }
            result[i] = arg;
        }
        return result;
    }

    public void setParamTypes(Class[] paramTypes) {
        paramTypesName = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class type = paramTypes[i];
            paramTypesName[i] = type.getName();
        }
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public HttpServiceKey getKeyToStatefullService() {
        return keyToStatefullService;
    }

    public void setKeyToStatefullService(HttpServiceKey keyToStatefullService) {
        this.keyToStatefullService = keyToStatefullService;
    }
}