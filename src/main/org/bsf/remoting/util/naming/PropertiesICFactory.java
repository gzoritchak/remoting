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
 */
package org.bsf.remoting.util.naming;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class allows to create an Initial context depending on a
 * property file. The normal use is to set the properties ressources and then to call
 * the getInitialContext() that will returns the cached initial context.
 *
 * However, another solution is to call the static method getInitialContext(String )
 * that allows the use of different Initial contexts in the same JVM
 *
 * This is usefull when more than one IC can be used
 * in the same JVM.
 *
 * @author Gaetan Zoritchak
 * @version@
 */
public class PropertiesICFactory implements InitialContextFactory {

    private String _propertiesRessources;
    private Context _ic;

    /**
     * sets the properties ressource and load the Initial context
     * corresponding to the ressource.
     */
    public void setPropertiesRessources(String propertiesRessources) {
        boolean bTmpCheckResult = false;

        if ( ( propertiesRessources != null ) && (
                !propertiesRessources.equals( "") ) )
            bTmpCheckResult = true;

        if ( !bTmpCheckResult)
            throw new IllegalArgumentException();
        _propertiesRessources = propertiesRessources;
        loadICInstance();
    }

    /**
     * Instantiates the Initial context according to the properties.
     */
    private void loadICInstance() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(_propertiesRessources);

        if ( inputStream == null)
            throw new RuntimeException("Unable to load " + _propertiesRessources);

        Properties properties = new Properties();

        try {
            properties.load(inputStream);
            _ic = new InitialContext(properties);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * @return the cached initial context.
     */
    public Context getInitialContext() {
        if (_ic == null )
            throw new RuntimeException("The properties name haven't been set.");
        return _ic;
    }

    /**
     * Creates a new Initial context using the ressource properties.
     */
    public static Context createInitialContext(String propertiesRessource) {
        Context result = null;

        Properties properties = new Properties();

        //If a properties file resource was specified in the servlet context,
        //we use it to initialize the properties
        if ( propertiesRessource != null && propertiesRessource.length() > 0){
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(propertiesRessource);

            if ( inputStream == null)
                throw new RuntimeException("Unable to load " + propertiesRessource);


            try {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }

        try {
            result = new InitialContext(properties);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return result;
        }

}