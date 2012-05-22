/**
 * Public domain  (does not reach threshold of originiality)
 * 
 * Author:  Mattias Andrée, maandree@kth.se
 * Year:    2012
 */
//set package

import java.io.*;


/**
 * UTF8 to UTF32 convertion stream
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class DecodeStream extends OutputStream
{
    public DecodeStream(final OutputStream next)
    {
	this.next = next;
    }
	
    private final OutputStream next;
    private int n = 0;
    private int buf = 0;
	
    public void write(final int b) throws IOException
    {
	if ((b & 0x80) == 0)
	    this.next.write(b);
	else if ((b & 0xC0) == 0xC0)
	{
	    n = 0;
	    buf = b;
	    while ((buf & 0x80) == 0x80)
	    {
		n++;
		buf <<= 1;
	    }
	    buf = (buf & 0xFF) >> n--;
	}
	else
	    if (n > 0)
	    {
		buf = (buf << 6) | (b & 63);
		if (--n == 0)
		    this.next.write(buf);
	    }
    }
    
    public void flush() throws IOException
    {
	this.next.flush();
    }
    
}
