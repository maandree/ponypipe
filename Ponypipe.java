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
	    encodePipe.write(b);
    }
    
    
    public static class DecodeStream extends OutputStream
    {
	public DecodeStream(final OutputStream next)
	{
	    this.next = next;
	}
	
	private final OutputStream next;
	
	public void write(final int b) throws IOException
	{
	    next.write(b);
	}
    }
    
    
    public static class EncodeStream extends OutputStream
    {
	public EncodeStream(final OutputStream next)
	{
	    this.next = next;
	}
	
	private final OutputStream next;
	
	public void write(final int b) throws IOException
	{
	    next.write(b);
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

