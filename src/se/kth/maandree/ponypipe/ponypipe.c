#include <string.h>
#include <stdio.h>
#include <stddef.h>


#define BLOCK_SIZE  (8 << 10)


static int buffered_read(FILE* f, size_t block_size, int (*sink)(char* buf, size_t n));
static int load_rules(char* buf, size_t n);
static int add_rule(char* buf, size_t n);


/**
 * Mane!
 * 
 * @param   argc  The number of elements in `argv`
 * @param   argv  Command line arguments
 * @return        Zero on success
 */
int main(int argc, char** argv)
{
  const char* rules = "./rules";
  int ponify = 1;
  int i;
  FILE* f;
  
  /* Parse command line options. */
  for (i = 1; i < argc; i++)
    {
      char* arg = *(argv + i);
      if (!strcmp(arg, "--ponify") || !strcmp(arg, "-z") || !strcmp(arg, "-p"))
	ponify = 1;
      else if (!strcmp(arg, "--deponify") || !strcmp(arg, "-d"))
	ponify = 0;
      else if (!strcmp(arg, "--rule") || !strcmp(arg, "--rules") || !strcmp(arg, "-r"))
	if (i + 1 < n)
	  rules = (const char*)*(args + ++i);
    }
  
  /* Open the rules file. */
  f = fopen(rules, "r");
  if (f == NULL)
    {
      perror(*argv);
      return 1;
    }
  
  /* Parse rules file. */
  if (buffered_read(f, BLOCK_SIZE, load_rules))
    {
      perror(*argv);
      /* TODO release resource. */
      return 1;
    }
  
  /* Close the rules file. */
  fclose(f);
  
  /* TODO */
  
  return 0;
}


/**
 * Read a file in a buffered manner
 * 
 * @param   f           The file to read from
 * @param   block_size  The number of bytes to read at each I/O operation
 * @param   sink        The function to which to send the read data
 * @return              Non-zero on error
 */
static int buffered_read(FILE* f, size_t block_size, int (*sink)(char* buf, size_t n))
{
  char* buf = malloc(block_size * sizeof(char));
  int rc = 0;
  size_t r;
  
  if (buf == NULL)
    return -1;
  
  for (;;)
    {
      /* Read a block. */
      r = fread(buf, sizeof(char), block_size, f);
      
      /* Look for read error. */
      if ((r < block_size) && ferror(f))
	{
	  sink(NULL, 1)
	  rc = -1;
	  break;
	}
      
      /* Send the block to the sink. */
      if (sink(buf, r))
	{
	  rc = -1;
	  break;
	}
      
      /* Check if the end of the file was reached. */
      if ((r < block_size) && feof(f))
	{
	  sink(NULL, 0);
	  break;
	}
    }
  
  free(buf);
  return rc;
}


/**
 * Parse a read chunk of data from a rule file
 * 
 * @param   buf  The read data, `NULL` on error or end of file
 * @param   n    The number of read characters, if `NULL` zero marks end of file, non-zero marks error
 * @return       Non-zero on error
 */
static int load_rules(char* buf, size_t n)
{
  static char* rule_buf = NULL;
  static size_t buf_size = 128;
  static size_t ptr = 0;
  static int comment = 0;
  
  /* Allocate the single rule-buffer the first time this function is called. */
  if (rule_buf == NULL)
    {
      rule_buf = malloc(buf_size * sizeof(char));
      if (rule_buf == NULL)
	return -1;
    }
  
  if (buf == NULL)
    {
      /* End of file has been reach, or an error has occurred. */
      if (n == 0) /* There was no error. */
	/* Parse and add rule. */
	if (add_rule(buf, ptr))
	  {
	    free(rule_buf);
	    return -1;
	  }
      free(rule_buf);
    }
  else
    {
      /* Parse the read data. */
      size_t i;
      for (i = 0; i < n; i++)
	{
	  char c = *(buf + i);
	  if ((ptr == 0) && (c == '#'))
	    {
	      /* The line is a comment. */
	      comment = 1;
	      ptr = 1;
	    }
	  else if (comment)
	    {
	      /* A comment stops at the end it the line it started at the beginning of.  */
	      if (c == '\n')
		{
		  ptr = 0;
		  comment = 0;
		}
	    }
	  else if (c == '\n')
	    {
	      /* Parse and add the rule when the end of the rule line has been reached. */
	      if (add_rule(buf, ptr))
		{
		  free(rule_buf);
		  return -1;
		}
	      ptr = 0;
	    }
	  else
	    {
	      /* Grow the rule buffer if it is too small for the current rule line. */
	      if (ptr == buf_size)
		{
		  char* new_buf = realloc(rule_buf, (buf_size <<= 1) * sizeof(char));
		  if (new_buf == NULL)
		    {
		      free(rule_buf);
		      return -1;
		    }
		  rule_buf = new_buf;
		}
	      /* Add the current character to the rule buffer.  */
	      *(rule_buf + ptr++) = c;
	    }
	}
    }
  
  return 0;
}


static int add_rule(char* buf, size_t n)
{
  /* TODO */
  return 0;
}

