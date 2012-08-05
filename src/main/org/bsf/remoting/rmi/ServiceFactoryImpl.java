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
package org.bsf.remoting.rmi;

import org.apache.commons.logging.*;
import org.bsf.remoting.ServiceFactory;
import org.bsf.remoting.EJBDefinition;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.lang.reflect.Method;
import java.util.Hashtable;

public class ServiceFactoryImpl implements ServiceFactory {

    private static Log log = LogFactory.getLog(ServiceFactoryImpl.class);

    private Hashtable services = new Hashtable();

    private static Context context = null;

    public ServiceFactoryImpl() {
    }

    public Object getService(EJBDefinition service) {
        if (service == null) throw new RuntimeException();
        Object result = services.get(service);
        if (result == null) {
            result = createService(service);
            services.put(service, result);
        }
        return result;
    }

    private Object createService(EJBDefinition service) {
        Object result = null;
        try {
            String p_sEJBName = service.getJndiName();
            Object objref =  getContext().lookup(p_sEJBName);
            EJBHome home = (EJBHome) PortableRemoteObject.narrow( objref, service.getHomeClass() );
            Method create = service.getHomeClass().getDeclaredMethod("create", new Class[0]);
            result = create.invoke(home, new Object[0]);
        } catch (Exception e) {
            log.fatal("Unable to lookup the service " + service.toString(), e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return result;
    }

    private static Context getContext(){
        if (context == null){
            try {
                context = new InitialContext();
            } catch (NamingException e) {
                log.fatal(e.getLocalizedMessage());
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }

        return context;
    }




}