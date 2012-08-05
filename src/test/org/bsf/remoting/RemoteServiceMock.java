package org.bsf.remoting;

import java.io.InputStream;
import java.io.IOException;

import org.bsf.remoting.BSFImage;
import org.bsf.remoting.RemoteService;

public class RemoteServiceMock implements RemoteService{

    private int statefullIndex = 0;

    public int compute(int varA, int varB, int operator) {
        int result = 0;
        switch (operator) {
            case OPERATOR_ADD :
                result = varA + varB;
                break;
            case OPERATOR_MINUS:
                result = varA - varB;
                break;
            case OPERATOR_MULT:
                result = varA * varB;
                break;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        return result;
    }

    public void throwsException() throws Exception {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
        throw new Exception("New Exception message");
    }

    public String upperWord(String word) {
        if (word == null)
            return null;
        sleep100();
        return word.toUpperCase();
    }

    private void sleep100() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}
    }


    /**
     * Loads the image from the ressources
     * @return
     */
    public BSFImage getBSFImage(){
        //The max size must be 100ko.
        byte[] buffer = new byte[1024*100];
        byte[] data = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream("images/TeamImage.jpg");
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
        sleep100();
        return new BSFImage("BSF Architecture Team", data);
    }


    public String createStatefull() {
        statefullIndex++;
        sleep100();
        return "EJB" + statefullIndex;
    }

    public String remoteStatefulCall(){
        sleep100();
        return "CallOnEJB" + statefullIndex;
    }
}
