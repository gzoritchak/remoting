package org.bsf.remoting;

import org.bsf.commons.ejb.SessionAdapterBean;
import org.bsf.commons.ejb.EJBFactory;

import javax.ejb.CreateException;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * This Test EJB is used by the JUnit test and by the BF remoting demo.
 *
 *  @ejb:bean       type="Stateless"
 *                  name="StatelessTest"
 *                  jndi-name="ejb/TestSession"
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
 *      ejb-name="StatelessTest"
 *      jndi-name="ejb/TestSession"
 */
public class StatelessTestBean extends SessionAdapterBean {

    private static int exceptionCount = 0;

    private List images = new ArrayList();
    private Random random = null;

    /**
     * @ejb:interface-method
     * @return the caller principal set on the server
     */
    public String getCallerPrincipal() {
        String name = _ejbContext.getCallerPrincipal().getName();
        logGraphBegin("getCallerPrincipal : " + name);
        return name;
    }


    /**
     * This method returns the given word in upper case to test a basic call.
     * @ejb:interface-method
     */
    public String upper(String word){
        return word.toUpperCase();
    }

    /**
     * This method returns the given word in upper case to test a basic call.
     * @ejb:interface-method
     */
    public int compute(int varA, int varB, int operator) {
        int result = 0;
        switch (operator) {
            case RemoteService.OPERATOR_ADD :
                result = varA + varB;
                break;
            case RemoteService.OPERATOR_MINUS:
                result = varA - varB;
                break;
            case RemoteService.OPERATOR_MULT:
                result = varA * varB;
                break;
        }
        return result;
    }


    /**
     * This method always throws an exception. The message varies based on
     * the thrown exceptions count.
     * @throws Exception
     *
     * @ejb:interface-method
     */
    public void throwsException() throws Exception {
        throw new Exception("Exception "+ exceptionCount++);
    }

    /**
     * This method always throws an SecurityException.
     * As this exception is a RunTimeException, the container should
     * encapsulate it in a EJBException
     *
     * @ejb:interface-method
     */
    public void throwsSecurityException() {
        throw new SecurityException("No rights for this call");
    }


    /**
     * This method is used to simulate the creation of a statefull bean and to
     * test it.
     * @ejb:interface-method
     */
    public StatelessTest createOtherBean(){
        return (StatelessTest) getEJBContext().getEJBObject();
    }


    /**
     * This method is used to simulate the creation of a statefull bean and to
     * test it.
     *
     * @ejb:interface-method
     */
    public StatefulTest createStateful(){
        StatefulTest result = null;

        try {
            result = ((StatefulTestHome)
                    EJBFactory.getHome(StatefulTestHome.class)).create();
        } catch (Exception e) {
            throw  new RuntimeException(e.getLocalizedMessage());
        }
        return result;
    }


    /**
     * Loads the image from the ressources
     * @return
     *
     * @ejb:interface-method
     */
    public BSFImage getBSFImage(){
        int i = random.nextInt(images.size());
        return (BSFImage) images.get(i);
    }

    private byte[] loadImageData(String resourceName){
        //The max size must be 100ko.
        byte[] buffer = new byte[1024*100];
        byte[] data = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream(resourceName);
        int i = 0;
        while(true){
            int nextByte = 0;
            try {
                nextByte = is.read();
            } catch (IOException e) {
                throw new RuntimeException(e.getLocalizedMessage());
            }
            if (nextByte == -1){
                data = new byte[i];
                System.arraycopy(buffer, 0, data, 0, i);
                break;
            }
            buffer[i] = (byte)nextByte;
            i++;
        }
        return data;
    }


    /**
     *  @ejb.create-method
     */
    public void ejbCreate() throws CreateException {
        BSFImage newImage =new BSFImage("BSF Architecture Team",
                loadImageData("images/TeamImage.jpg"));

        images.add(newImage);

        newImage = new BSFImage("Our President",
                loadImageData("images/Hendrix7.png"));
        images.add(newImage);

        newImage = new BSFImage("Our GUI Expert",
                loadImageData("images/keithRichards.jpg"));
        images.add(newImage);

        random = new Random();

    }

}
