/**
 * Public domain  (does not reach threshold of originiality)
 * 
 * Author:  Mattias Andrée, maandree@kth.se
 * Year:    2012
 */
//set package

import java.io.*;


/**
 * UTF32 to UTF8 convertion stream
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class EncodeStream extends OutputStream
{
    public EncodeStream(final OutputStream next)
    {
	this.next = next;
    }
    
    private final OutputStream next;
    private final int[] buf = new int[6];
    
    public void write(final int b) throws IOException
    {
	if (b < 0x80)
	    this.next.write(b);
	else
	{
	    int m = 0x100;
	    int d = b;
	    int ptr = 0;
	    for (;;)
	    {
		m |= m >> 1;
		this.buf[ptr++] = d & 63;
		d >>>= 6;
		if (d == 0)
		{
		    m >>= 1;
		    if ((m & this.buf[ptr - 1]) == 0)
			this.buf[ptr - 1] |= (m << 1) & 0xFF;
		    else
			this.buf[ptr++] = m;
		    break;
		}
	    }
	    
	    while (ptr > 0)
		this.next.write(this.buf[--ptr]);
	}
    }
    
    public void flush() throws IOException
    {
	this.next.flush();
    }
}

