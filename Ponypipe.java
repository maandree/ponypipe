/**
 * Public domain  (does not reach threshold of originiality)
 * 
 * Author:  Mattias Andrée, maandree@kth.se
 * Year:    2012
 */
//set package

import java.io.*;


/**
 * Main class!
 * 
 * @author  Mattias Andrée, <a href="maandree@kth.se">maandree@kth.se</a>
 */
public class Ponypipe
{
    public static void main(final String... args) throws Throwable
    {
	Boolean _ponify = null;
	boolean deponify = false;
	String rules = "/usr/share/ponypipe";
	
	for (int i = 0, n = args.length; i < n; i++)
	    if (args.equals("-z") || args.equals("--ponify"))
		_ponify = Boolean.TRUE;
	    else if (args.equals("-d") || args.equals("--deponify"))
		deponify = true;
	    else if (args.equals("-r") || args.equals("--rule") || args.equals("--rules"))
		if (i + 1 < n)
		    rules = args[++i];
	
	final boolean ponify = (_ponify == null) || (deponify == false);
	
	// load rules
	
	// stdin > decode:-utf8 > ponify > deponify > encode:+utf8 > stdout
	
	final OutputStream encodePipe   = new EncodeStream(System.out);
	final OutputStream deponifyPipe = deponify ? new DeponifyStream(encodePipe) : null;
	final OutputStream ponifyPipe   = ponify ? new PonifyStream(deponify ? deponifyPipe : encodePipe) : null;
	final OutputStream decodePipe   = new DecodeStream(ponify ? ponifyPipe : deponify ? deponifyPipe : encodePipe);
	
	for (int b; (b = System.in.read()) != -1;)
	    decodePipe.write(b);
    }
    
    
    public static class DecodeStream extends OutputStream
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
		    buf = (buf << 6) | (b & 1023);
		    if (--n == 0)
			this.next.write(buf);
		}
	}
    }
    
    
    public static class EncodeStream extends OutputStream
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
		    this.buf[ptr++] = d & 1023;
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
		
		this.buf[0] = this.buf[1] = this.buf[2] = this.buf[3] = this.buf[4] = this.buf[5] = 0;
	    }
	}
    }
    
    
    public static class PonifyStream extends OutputStream
    {
	public PonifyStream(final OutputStream next)
	{
	    this.next = next;
	}
	
	private final OutputStream next;
	
	public void write(final int b) throws IOException
	{
	    next.write(b);
	}
    }
    
    
    public static class DeponifyStream extends OutputStream
    {
	public DeponifyStream(final OutputStream next)
	{
	    this.next = next;
	}
	
	private final OutputStream next;
	
	public void write(final int b) throws IOException
	{
	    next.write(b);
	}
    }
    
}

