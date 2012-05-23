/**
 * Public domain  (does not reach threshold of originiality)
 * 
 * Author:  Mattias Andrée, maandree@kth.se
 * Year:    2012
 */
package se.kth.maandree.ponypipe;

import java.io.*;


/**
 * Mane class!
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Ponypipe //Who care's if it is fast!
{   public static void main(final String... args) throws Throwable
    {   Boolean _ponify = null;
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
	
	{   // rulesIn > decodePipe > rulesPipe
	    
	    final OutputStream rulesPipe  = new RulesStream();
	    final OutputStream decodePipe = new DecodeStream(rulesPipe);
	    final InputStream rulesIn     = new BufferedInputStream(new FileInputStream(new File(rules)));
	    
	    for (int b; (b = rulesIn.read()) != -1;)
		decodePipe.write(b);
	    decodePipe.flush();
	}
	
	{   final int[][] tmp = new int[hpPtr][];
	    System.arraycopy(humans, 0, tmp, 0, hpPtr);
	    humans = tmp;
	}
	{   final int[][] tmp = new int[hpPtr][];
	    System.arraycopy(ponies, 0, tmp, 0, hpPtr);
	    ponies = tmp;
	}
	
	{   // stdin > decode:-utf8 > ponify > deponify > encode:+utf8 > stdout
	    
	    final OutputStream encodePipe   = new EncodeStream(System.out);
	    final OutputStream deponifyPipe = deponify ? new DeponifyStream(encodePipe) : null;
	    final OutputStream ponifyPipe   = ponify ? new PonifyStream(deponify ? deponifyPipe : encodePipe) : null;
	    final OutputStream decodePipe   = new DecodeStream(ponify ? ponifyPipe : deponify ? deponifyPipe : encodePipe);
	
	    for (int b; (b = System.in.read()) != -1;)
		decodePipe.write(b);
	    decodePipe.flush();
	}
    }
    
    
    
    static int[][] humans = new int[16][];
    static int[][] ponies = new int[16][];
    static int hpPtr = 0;
    
    
    
    static void addRule(final int[] data, final int len)
    {   int[] tmp = new int[data.length];
	for (int i = 1, n = len - 2; i < n; i++)
	    if ((data[i] == ':') && (data[i + 1] == ':') && (data[i - 1] == ' ') && (data[i + 2] == ' '))
	    {   final int[] human, pony;
		{   int last = ' ';
		    int ptr = 0, c;
		    for (int j = 0; j < i; j++)
			if (((c = data[j]) != ' ') || (last != ' '))
			    tmp[ptr++] = last = c;
		    if (ptr == 0)     return;
		    if (last == ' ')  ptr--;
		    human = new int[ptr];
		    System.arraycopy(data, 0, human, 0, ptr);
		}
		{   int last = ' ';
		    int ptr = 0, c;
		    for (int j = i + 3; j < len; j++)
			if (((c = data[j]) != ' ') || (last != ' '))
			    tmp[ptr++] = last = c;
		    if (ptr == 0)     return;
		    if (last == ' ')  ptr--;
		    pony = new int[ptr];
		    System.arraycopy(data, i, pony, 0, ptr);
		}
		
		if (hpPtr == humans.length)
		{   int[][] _tmp;
		    
		    _tmp = new int[hpPtr << 1][];
		    System.arraycopy(humans, 0, _tmp, 0, hpPtr);
		    humans = _tmp;
		    
		    _tmp = new int[hpPtr << 1][];
		    System.arraycopy(ponies, 0, _tmp, 0, hpPtr);
		    ponies = _tmp;
		}
		
		humans[hpPtr] = human;
		ponies[hpPtr] = pony;
		hpPtr++;
    }       }
    
    
    public static class PonifyStream extends TranslateStream
    {   public PonifyStream(final OutputStream next)
	{    super(next, humans, ponies);
    }   }
    
    
    public static class DeponifyStream extends TranslateStream
    {   public DeponifyStream(final OutputStream next)
	{   super(next, ponies, humans);
    }   }
        
    
    public static class RulesStream extends OutputStream
    {   private int bufSize = 128;
	private int[] buf = new int[bufSize];
	private int ptr = 0;
	private boolean comment = false;
	
	public void write(final int b) throws IOException
	{   if ((this.ptr == 0) && (b == '#'))
	    {   this.comment = true;
		this.ptr = 1;
	    }
	    else if (this.comment)
	    {   if (b == '\n')
		{   this.ptr = 0;
		    this.comment = false;
	    }   }
	    else if (b == '\n')
	    {	Ponypipe.addRule(buf, ptr);
		ptr = 0;
	    }
	    else
	    {	if (ptr == bufSize)
		{   final int[] nbuf = new int[this.bufSize <<= 1];
		    System.arraycopy(this.buf, 0, nbuf, 0, this.bufSize >> 1);
		    this.buf = nbuf;
		}
		this.buf[this.ptr++] = b;
	}   }
	
	
	public void flush() throws IOException
	{   this.write('\n');
}   }   }

