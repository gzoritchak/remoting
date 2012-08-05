package org.bsf.remoting;

import java.io.Serializable;

/**
 * Holds informations on an image and the bytes composing the underlying
 * image.
 */
public class BSFImage implements Serializable{

    String imageName;
    byte[] imageData;

    public BSFImage(String imageName, byte[] imageData) {
        this.imageName = imageName;
        this.imageData = imageData;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

}
