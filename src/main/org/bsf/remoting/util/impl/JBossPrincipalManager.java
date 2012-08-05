/**
 * @licence.bsf@
 */
package org.bsf.remoting.util.impl;

import org.bsf.remoting.util.DefaultPrincipal;
import org.bsf.remoting.util.PrincipalManager;
import org.jboss.security.ClientLoginModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.Hashtable;

/**
 * @author Gaetan Zoritchak
 * @version@
 */
public class JBossPrincipalManager implements PrincipalManager{

    private static Log log = LogFactory.getLog(JBossPrincipalManager.class);


    public void setThreadPrincipal(String login, String password) {
        Principal principal = new DefaultPrincipal(login);
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        LoginModule loginModule = new ClientLoginModule();

        Hashtable options = new Hashtable(2);
        options.put("multi-thread", "true");
        options.put("password-stacking", "useFirstPass");

        Hashtable sharedState = new Hashtable(2);
        sharedState.put("javax.security.auth.login.name", login);
        sharedState.put("javax.security.auth.login.password", password);

        loginModule.initialize(subject, null, sharedState, options);

        try {
            loginModule.login();
        } catch (LoginException e) {
            String msg = "Fatal error during the set of the principal for " + login;
            log.fatal(msg,e);
            throw new RuntimeException( msg + " : " + e.getLocalizedMessage());
        }

    }

    public void setJVMPrincipal(String login, String password) {
        throw new UnsupportedOperationException("Not implemented");
    }
}