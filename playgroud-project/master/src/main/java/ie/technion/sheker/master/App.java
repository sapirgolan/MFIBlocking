package ie.technion.sheker.master;

import org.depandent.DepandentApp;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        System.out.println("Hi, This is master project: " + App.class.getName());
        System.out.println("Calling dependant project");
        DepandentApp.print();
    }
}
