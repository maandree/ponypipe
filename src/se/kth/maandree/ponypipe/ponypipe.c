#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>


#define BLOCK_SIZE  (8 << 10)


static int buffered_read(FILE* f, size_t block_size, int (*sink)(char* buf, size_t n));
static int load_rules(char* buf, size_t n);
static int add_rule(char* buf, size_t n);



/**
 * The rule lines that should be free:d
 */
static char** rules = NULL;

/**
 * The human words
 */
static char** humans = NULL;

/**
 * The pony words
 */
static char** ponies = NULL;

/**
 * The number of rules added to the table
 */
static size_t rules_ptr = 0;

/**
 * The capacity of the rule table
 */
static size_t rules_size = 128;



/**
 * Mane!
 * 
 * @param   argc  The number of elements in `argv`
 * @param   argv  Command line arguments
 * @return        Zero on success
 */
int main(int argc, char** argv)
{
  const char* rules_file = "./rules";
  int ponify = 1;
  FILE* f = NULL;
  int rc = 0;
  int i;
  
  /* TODO support, and default to, precompiled rules */
  
  /* Parse command line options. */
  for (i = 1; i < argc; i++)
    {
      char* arg = *(argv + i);
      if (!strcmp(arg, "--ponify") || !strcmp(arg, "-z") || !strcmp(arg, "-p"))
	ponify = 1;
      else if (!strcmp(arg, "--deponify") || !strcmp(arg, "-d"))
	ponify = 0;
      else if (!strcmp(arg, "--rule") || !strcmp(arg, "--rules") || !strcmp(arg, "-r"))
	if (i + 1 < argc)
	  rules_file = (const char*)*(argv + ++i);
    }
  
  
  /* Allocate memories for rules */
  if ((rules  = malloc(rules_size * sizeof(char*))) == NULL)  goto fail;
  if ((humans = malloc(rules_size * sizeof(char*))) == NULL)  goto fail;
  if ((ponies = malloc(rules_size * sizeof(char*))) == NULL)  goto fail;
  
  
  /* Open the rules file. */
  if ((f = fopen(rules_file, "r")) == NULL)
    goto fail;
  
  /* Parse rules file. */
  if (buffered_read(f, BLOCK_SIZE, load_rules))
    goto fail;
  
  /* Close the rules file. */
  fclose(f);
  f = NULL;
  
  
  /* TODO */
  
  
 done:
  /* Release everything, do both on error and on success. */
  if (f != NULL)
    fclose(f);
  while (rules_ptr > 0)
    if (rules[--rules_ptr] != NULL)
      free(rules[rules_ptr]);
  if (rules  != NULL)  free(rules);
  if (humans != NULL)  free(humans);
  if (ponies != NULL)  free(ponies);
  
  /* Exit the program. */
  return rc;
  
  
 fail:
  perror(*argv);
  rc = 1;
  goto done;
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
	  sink(NULL, 1);
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
	      /* Make sure that `add_rule` can add a NUL-termination. */
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
	      *(rule_buf + ptr++) = (c == '\t' ? ' ' : c);
	    }
	}
    }
  
  return 0;
}


/**
 * Add a rule to the rule table
 * 
 * @param   buf  The rule
 * @param   n    The length of the rule, this excludes one extra char in the end that should be used for NUL-termination
 * @return       Non-zero on error
 */
static int add_rule(char* buf, size_t n)
{
  char* delimiter;
  char* rule;
  char* human;
  char* pony;
  size_t m;
  
  /* NUL-terminate the buffer and the last pattern. */
  buf[n] = '\0';
  
  /* Find human–pony delimiter. */
  delimiter = strstr(buf, " :: ");
  if (delimiter == NULL)
    return 0;
  
  /* NUL-terminate the first pattern. */
  *delimiter = '\0';
  
  /* Copy the buffer. */
  if ((rule = malloc((n + 1) * sizeof(char))) == NULL)
    return -1;
  memcpy(rule, buf, n + 1);
  
  /* Get the human pattern and the pony pattern. */
  m = (delimiter - buf);
  human = rule;
  pony = human + m + 4;
  
  /* Strip left-side spaces. */
  while (*human == ' ')  human++;
  while (*pony  == ' ')  pony++;
  
  /* Strip right-side spaces. */
  while (human[--m] == ' ')  ;
  while (pony[--n]  == ' ')  ;
  human[m + 1] = '\0';
  pony[n + 1]  = '\0';
  
  /* Grow rule table if neccessary. */
  if (rules_ptr == rules_size)
    {
      char** new_buf;
      rules_size <<= 1;
      
      if ((new_buf = realloc(rules, rules_size * sizeof(char*))) == NULL)  return -1;
      rules = new_buf;
      
      if ((new_buf = realloc(humans, rules_size * sizeof(char*))) == NULL)  return -1;
      humans = new_buf;
      
      if ((new_buf = realloc(ponies, rules_size * sizeof(char*))) == NULL)  return -1;
      ponies = new_buf;
    }
  
  /* Add rules to table. */
  rules[rules_ptr] = rule;
  humans[rules_ptr] = human;
  ponies[rules_ptr] = pony;
  rules_ptr++;
  
  return 0;
}

