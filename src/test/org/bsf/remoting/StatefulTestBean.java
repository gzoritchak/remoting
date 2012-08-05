package org.bsf.remoting;

import org.bsf.commons.ejb.SessionAdapterBean;

import javax.ejb.CreateException;

/**
 * EJB de test
 *
 *  @ejb:bean       type="Stateful"
 *                  name="StatefulTest"
 *                  jndi-name="ejb/StatefulTest"
 *                  generate="true"
 *                  view-type="remote"
 *
 *  @ejb:home       extends="javax.ejb.EJBHome"
 *                  generate="remote"
 *
 *  @ejb:interface  extends="javax.ejb.EJBObject"
 *                  generate="remote"
 *
 *  @ejb:transaction type="Required"
 *
 *  @jonas.bean
 *      ejb-name="StatefulTest"
 *      jndi-name="ejb/StatefulTest"
 */
public class StatefulTestBean extends SessionAdapterBean {

    private static int eJBCount = 0;

    /**
     * The EJB Name
     */
    private String name = null;

    private int callCount = 1;


    public void ejbCreate() throws CreateException {
        super.ejbCreate();
        name = "EJB"+ eJBCount++;
    }

    /**
     * @return the name of the current EJB
     *
     * @ejb:interface-method
     */
    public String getEJBName(){
        return name;
    }

    /**
     * @return the name of the current EJB
     *
     * @ejb:interface-method
     */
    public String call(){
        return "call " + callCount++ + " on "+ name;
    }


}
