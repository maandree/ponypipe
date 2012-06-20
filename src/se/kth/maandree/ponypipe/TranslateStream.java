/**
 * Public domain  (does not reach threshold of originiality)
 * 
 * Author:  Mattias Andrée, maandree@kth.se
 * Year:    2012
 */
package se.kth.maandree.ponypipe;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Vector;


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
    private int last = 0;
    private final ArrayDeque<Vector<Integer>> whitespaces = new ArrayDeque<Vector<Integer>>();
    
    
    public void write(final int _b) throws IOException
    {
	/* Multiply whitespaces */
	
	int b = _b;
	if (Character.isWhitespace(b))
	{
	    if (this.last != ' ')
		whitespaces.offerLast(new Vector<Integer>());
	    whitespaces.peekLast().add(Integer.valueOf(b));
	    b = ' ';
	    if (this.last == ' ')
		return;
	}
	this.last = b;
	
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
		    (Character.isWhitespace($from[this.ptr]) && Character.isWhitespace(b))     // whitespace
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
					else
					    { kk--; continue; }
					break;
				    }
				    
				    write(this.next, chr);
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
	    write(this.next, this.buf[0]);
	    final int[] cbuf = new int[this.buf.length];
	    System.arraycopy(this.buf, 0, cbuf, 0, this.ptr);
	    int n = this.ptr;
	    this.ptr = 0;
	    for (int i = 1; i < n; i++)
		write(this, cbuf[i]);
	}
    }
    
    private void write(final OutputStream out, final int chr) throws IOException
    {
	this.last = 0;
	if (chr != ' ')
	    out.write(chr);
	else
	{
	    final Vector<Integer> wss = whitespaces.pollFirst();
	    if (wss == null)
		out.write(' ');
	    else
		for (final Integer ws : wss)
		    out.write(ws.intValue());
	}
    }
    
    public void flush() throws IOException
    {   
	for (Vector<Integer> wss; (wss = whitespaces.pollFirst()) != null;)
	    for (final Integer ws : wss)
		this.next.write(ws.intValue());
	this.next.flush();
}   }
