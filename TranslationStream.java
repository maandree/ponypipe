/**
 * Public domain  (does not reach threshold of originiality)
 * 
 * Author:  Mattias Andrée, maandree@kth.se
 * Year:    2012
 */
//set package

import java.io.*;


/**
 * Abstract translation stream
 * 
 * @author  Mattias Andrée,  <a href="maailto:maandree@kth.se">maandree@kth.se</a>
 */
public class TranslateStream extends OutputStream
{   public TranslateStream(final OutputStream next, final int[][] from, final int[][] to)
    {   this.next = next;
    }
    
    private final OutputStream next;
    
    public void write(final int b) throws IOException
    {
	this.next.write(b);
    }
    
    public void flush() throws IOException
    {   this.next.flush();
}   }
