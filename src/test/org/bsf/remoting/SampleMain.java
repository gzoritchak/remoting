package org.bsf.remoting;

import org.bsf.remoting.EJBDefinition;
import org.bsf.remoting.http.HttpServiceFactory;

/**
 * Shows the use of the Http Invocation to communication with an EJB.
 *
 */
public class SampleMain {

    /**
     * The factory
     */
    private static HttpServiceFactory factory =
        new HttpServiceFactory("localhost", 8080,"remoting");

        //For a secured communication we would have use this constructor :
        //new HttpServiceFactory("https","www.bs-factory.com", 443,"remoting");

    /**
     * Every access to a stateless EJB is defined through its EJBDefinition.
     * All the used EJB should be placed in a constant interface to facilitate
     * the view of all the services.
     */
    private static final EJBDefinition TEST_SESSION = new EJBDefinition(
        "ejb/TestSession","org.bsf.remoting.StatelessTestHome",
            "org.bsf.remoting.StatelessTest"
    );



    public static void main(String[] args) {

        //The retrieving of a stateless EJB is straightforward
        StatelessTest myService = (StatelessTest)
                factory.getService(TEST_SESSION);

        try {
            //Calls are made like any normal calls on an EJB.
            String upperWord = myService.upper("word");
            System.out.println("Result : " + upperWord);

            //The pattern to retrieve a statefull bean : we use a stateless Bean to
            //create it.
            StatefulTest myStateful = myService.createStateful();
            String name = myStateful.getEJBName();
            System.out.println("Statefull EJB Name : " + name);


            //We know want to know use the authentication
            factory.setLogin("myLogin");
            factory.setPassword("myPass");

            //All the following calls will use HTTP basic authentication
            if ("myLogin".equals(myService.getCallerPrincipal())){
                System.out.println("That's me");
            }


        } catch (java.rmi.RemoteException e) {
            e.printStackTrace();
        }
    }
}
