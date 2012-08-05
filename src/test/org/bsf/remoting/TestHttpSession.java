package org.bsf.remoting;

import org.bsf.remoting.EJBDefinition;
import org.bsf.remoting.http.HttpServiceFactory;
import junit.framework.TestCase;

import java.rmi.ServerException;

/**
 * Test the basic functionnalities of the HTTP invocation.
 * User: gaetan
 * Date: Dec 12, 2002
 * Time: 10:12:37 AM
 */
public class TestHttpSession extends TestCase{

    HttpServiceFactory factory;

    private static final EJBDefinition TEST_SESSION = new EJBDefinition(
        "ejb/TestSession",
            "org.bsf.remoting.StatelessTestHome",
            "org.bsf.remoting.StatelessTest"
    );

    public TestHttpSession(String s) {
        super(s);
        factory = new HttpServiceFactory("localhost", 8080, "remoting");
//        factory = new HttpServiceFactory("https","www.bs-factory.com", 443,"remoting");
    }

    public void testBasicCall() throws Exception {
        String result = getStatelessTest().upper("word");
        assertEquals("WORD",result);
    }

    public void testException() throws Exception{
        try {
            getStatelessTest().throwsException();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            //OK normal
            return;
        }
        fail("Exception should have been thrown");
    }


    public void testEJBException() throws Exception{
        try {
            getStatelessTest().throwsSecurityException();
        } catch (ServerException e) {

            System.out.println(e.getCause().getLocalizedMessage());
            //OK
            return;
        }
        fail("Exception should have been thrown");
    }


    public void testStatefullCreation() throws Exception {
        StatelessTest newRemoteObject = getStatelessTest().createOtherBean();
        assertNotNull(newRemoteObject);
        String result = newRemoteObject.upper("call_on_statefull");
        assertEquals(result, "CALL_ON_STATEFULL");
    }

    public void testAuthentification() throws Exception {
        factory.setLogin("titi");
        factory.setPassword("toto");

        long time = System.currentTimeMillis();
        assertEquals("titi", getStatelessTest().getCallerPrincipal());
        time = System.currentTimeMillis() - time;
        System.out.println("Call to getCurrentTimeMillis in " + time + " ms");
    }

    public StatelessTest getStatelessTest() throws Exception{
        return (StatelessTest) factory.getService(TEST_SESSION);
    }



}
