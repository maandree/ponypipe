/**
 * Public domain  (does not reach threshold of originiality)
 * 
 * Author:  Mattias Andrée, maandree@kth.se
 * Year:    2012
 */
package se.kth.maandree.ponypipe;

import java.io.*;


/**
 * Translation stream
 * 
 * @author  Mattias Andrée,  <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class TranslateStream extends OutputStream
{   public TranslateStream(final OutputStream next, final int[][] from, final int[][] to)
    {   this.next = next;
	this.from = from;
	this.to = to;
	alive = new int[from.length][];
	tmpalive = new int[from.length][];
    }
    
    
    private final OutputStream next;
    private final int[][] from;
    private final int[][] to;
    private int[] buf = new int[64];
    private int ptr = 0;
    private int[][] alive;
    private int[][] tmpalive;
    private int palive = 0;
    
    
    public void write(final int b) throws IOException
    {
	/* Store input text */
	
	if (this.ptr == buf.length)
	{
	    final int[] nbuf = new int[this.ptr];
	    System.arraycopy(this.buf, 0, nbuf, 0, this.ptr);
	    this.buf = nbuf;
	}
	buf[this.ptr] = b;
	
	/* Resurrect all alternatives on empty input buffer */
	
	if (this.ptr == 0)
	    System.arraycopy(this.from, 0, this.alive, 0, this.palive = this.from.length);
	
	/* Kill non-matching alternatives */
	
	int nalive = 0;
	for (int i = 0; i < this.palive; i++)
	{
	    final int[] $from = this.alive[i];
	    if ($from.length > this.ptr)
		if ((Character.toLowerCase ($from[this.ptr]) == Character.toLowerCase (b)) ||  // caseless match
		    (Character.isWhiteSpace($from[this.ptr]) && Character.isWhiteSpace(b))     // whitespace       //FIXME multiple whitespaces
		   )
		    if (this.ptr + 1 < $from.length)
			this.tmpalive[nalive++] = $from;
		    else
		    {
			/* Matching alternative found: translate and reset */
			
			for (int j = 0, n = this.from.length; j < n; j++)
			    if (this.from[j] == $from) // sic! : array identity matching
			    {
				final int[] $to = this.to[j];
				
				for (int k = 0, m = $to.length; k < m; k++)
				{
				    int chr = $to[k];
				    
				    /* Follow casing */
				    int kk = k < ptr ? k : (ptr - 1);
				    while (kk >= 0)
				    {
					if      (Character.isLowerCase(buf[kk]))  chr = Character.toLowerCase(chr);
					else if (Character.isUpperCase(buf[kk]))  chr = Character.toUpperCase(chr);
					else    { kk--; continue; }
					break;
				    }
				    
				    this.next.write(chr);
				}
				
				break;
			    }
			this.ptr = 0;
			return;
		    }
	}
	
	/* Update live alternatives */
	
	this.palive = nalive;
	int[][] tmp = this.tmpalive;
	this.tmpalive = this.alive;
	this.alive = tmp;
	
	/* Increase input buffer pointer */
	
	this.ptr++;
	
	/* Echo if all alternatives are dead */
	
	if (this.palive == 0)
	{
	    this.next.write(this.buf[0]);
	    final int[] cbuf = new int[this.buf.length];
	    System.arraycopy(this.buf, 0, cbuf, 0, this.ptr);
	    int n = this.ptr;
	    this.ptr = 0;
	    for (int i = 1; i < n; i++)
		this.write(cbuf[i]);
	}
    }
    
    public void flush() throws IOException
    {   this.next.flush();
}   }
