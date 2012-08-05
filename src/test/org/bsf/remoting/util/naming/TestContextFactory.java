/**
 * @licence.bsf@
 */
package org.bsf.remoting.util.naming;

import javax.naming.Context;

import junit.framework.TestCase;

/**
 * @author Gaetan Zoritchak
 * @version@
 */
public class TestContextFactory extends TestCase{

    public TestContextFactory(String s) {
        super(s);
    }

    public void testCreateContext() throws Exception {


        try {
            PropertiesICFactory.createInitialContext(null);
        } catch (Exception e) {
            assertTrue( e instanceof IllegalArgumentException);
        }

        try {
            PropertiesICFactory.createInitialContext("");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        try {
            PropertiesICFactory.createInitialContext("toto");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            System.out.println(e);
        }

        Context context = PropertiesICFactory.createInitialContext(
                "org/bsf/remoting/util/naming/jbossDefault.properties");

        assertNotNull(context);
        System.out.println(context);


    }
}