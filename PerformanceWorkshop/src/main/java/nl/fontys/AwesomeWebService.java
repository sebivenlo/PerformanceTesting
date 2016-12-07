package nl.fontys;

import java.util.Random;

public class AwesomeWebService {

    private final Random random;
    private AwesomeWebService proxy = this;

    public void setProxy(AwesomeWebService proxy) {
        this.proxy = proxy;
    }

    public AwesomeWebService() {
        random = new Random();
    }

    public int getAwesomeData() {
        int runs = random.nextInt(300);
        for (int i = 0; i < runs; i++) {
            proxy.magicMethod();
        }
        int result = random.nextInt();
        System.out.println("result = " + result);
        return result;
    }

    public void magicMethod() {
        //magical things happen here
        try {
            Thread.sleep(random.nextInt(10) + 5);
        } catch (InterruptedException ex) {
        }
    }
}
