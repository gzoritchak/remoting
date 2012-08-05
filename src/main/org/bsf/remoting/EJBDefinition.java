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
package org.bsf.remoting;

import java.io.Serializable;


/**
 * Defines the deployment settings of an EJB. It's home and remote interfaces...
 * @author Gaetan Zoritchak
 * @version 1.0
 */

public class EJBDefinition implements Serializable {

    private String _jndiName;
    private String _home;
    private String _remote;

    public EJBDefinition(String p_jndiName, String p_homeClassName,
                         String p_remoteClassName) {
        _jndiName = p_jndiName;
        _home = p_homeClassName;
        _remote = p_remoteClassName;
    }


    public Class getHomeClass() {
        try {
            return Class.forName(_home);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Unable to load the class : " + _home);
        }
    }

    public Class getRemoteClass() {
        try {
            return Class.forName(_remote);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Unable to load the class : " + _remote);
        }
    }

    public String getRemoteName() {
        return _remote;
    }

    public String getHomeName() {
        return _home;
    }

    public String getJndiName() {
        return _jndiName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof EJBDefinition) {
            EJBDefinition definition = (EJBDefinition) obj;
            if (!(definition.getHomeName().equals(_home))
                    || !(definition.getJndiName().equals(_jndiName))
                    || !(definition.getRemoteName().equals(_remote)))
                return false;
            return true;
        }
        return false;
    }

    public String toString() {
        return "EJB " + _jndiName + " Home class : " + _home;
    }

    public int hashCode() {
        return _jndiName.hashCode();
    }
}



