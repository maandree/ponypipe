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
	int n = from.length;
	this.from = new int[n][];
	this.to = new int[n][];
	for (int i = 0; i < n; i++)
	    wildcardFix(from[i], to[i], this.from, this.to, i);
	alive = new int[n][];
	tmpalive = new int[n][];
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
    
    
    
    public void wildcardFix(final int[] fromWord, final int[] toWord, final int[][] formArray, final int[][] toArray, final int index)
    {
	if ((fromWord[fromWord.length - 1] == '*') && (toWord[toWord.length - 1] == '*'))
	{
	    final int[] fw = new int[fromWord.length - 1];
	    final int[] tw = new int[toWord.length - 1];
	    System.arraycopy(fromWord, 0, fw, 0, fw.length);
	    System.arraycopy(toWord, 0, tw, 0, tw.length);
	    formArray[index] = fw;
	    toArray[index] = tw;
	}
	
	else if ((fromWord[fromWord.length - 1] == '*') && (toWord[toWord.length - 1] != '*'))
	{
	    //TODO what should be do here?
	}
	
	else if ((fromWord[fromWord.length - 1] != '*') && (toWord[toWord.length - 1] == '*'))
	{
	    final int[] fw = new int[fromWord.length + 1];
	    final int[] tw = new int[toWord.length];
	    System.arraycopy(fromWord, 0, fw, 0, fromWord.length);
	    System.arraycopy(toWord, 0, tw, 0, toWord.length);
	    (formArray[index] = fw)[fromWord.length] = ' ';
	    (toArray[index] = tw)[toWord.length - 1] = ' ';
	}
	
	else if ((fromWord[fromWord.length - 1] != '*') && (toWord[toWord.length - 1] != '*'))
	{
	    final int[] fw = new int[fromWord.length + 1];
	    final int[] tw = new int[toWord.length + 1];
	    System.arraycopy(fromWord, 0, fw, 0, fromWord.length);
	    System.arraycopy(toWord, 0, tw, 0, toWord.length);
	    (formArray[index] = fw)[fromWord.length] = ' ';
	    (toArray[index] = tw)[toWord.length] = ' ';
	}
    }
    
    public void write(final int _b) throws IOException
    {
	/* Multiply whitespaces */
	
	int b = _b;
	if ((b == '\0') || Character.isWhitespace(b))
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
	    int w;
	    final Vector<Integer> wss = whitespaces.pollFirst();
	    if (wss == null)
		out.write(' ');
	    else
		for (final Integer ws : wss)
		    if ((w = ws.intValue()) != '\0')
			out.write(w);
	}
    }
    
    public void flush() throws IOException
    {   
	int w;
	for (Vector<Integer> wss; (wss = whitespaces.pollFirst()) != null;)
	    for (final Integer ws : wss)
		    if ((w = ws.intValue()) != '\0')
			this.next.write(w);
	this.next.flush();
}   }
